package burp;

import javax.naming.Context;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BurpExtender implements IBurpExtender, IContextMenuFactory {
    private IExtensionHelpers helpers;
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        callbacks.setExtensionName("burp2wfuzz");
        helpers = callbacks.getHelpers();
        callbacks.registerContextMenuFactory(this);
    }

    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        byte context = invocation.getInvocationContext();
        if(context == IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_REQUEST ||
                context == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST){
            JMenuItem menuItem = new JMenuItem(new AbstractAction("Copy request as wfuzz command.") {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    copyToClipboard(actionEvent);
                }
            });
            menuItem.putClientProperty("wFuzzCommand", invocation);
            List<JMenuItem> menuItemsList = new ArrayList<>();
            menuItemsList.add(menuItem);
            return menuItemsList;
        }
        return null;
    }

    private String createWfuzzCommand(IContextMenuInvocation invocation){
        var request = invocation.getSelectedMessages()[0].getRequest();
        var service = invocation.getSelectedMessages()[0].getHttpService();

        IRequestInfo requestInfo = helpers.analyzeRequest(service, request);

        String url = requestInfo.getUrl().toString();

        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("wfuzz ");

        List<String> headers = requestInfo.getHeaders();
        headers = headers.subList(1, headers.size()-1);
        headers = headers.stream().map((x) -> String.format("-H '%s'", x)).collect(Collectors.toList());
        String wFuzzHeadersJoined = String.join(" ", headers);
        commandBuilder.append(wFuzzHeadersJoined);


        if(requestInfo.getMethod().equals("POST")){
            String requestString = helpers.bytesToString(request);
            int bodyOffset = requestInfo.getBodyOffset();
            String body = requestString.substring(bodyOffset);
            commandBuilder.append(String.format(" -d '%s' ", body));
        }

        commandBuilder.append("-c -w wordlist ");
        commandBuilder.append(url);

        return commandBuilder.toString();
    }

    private void copyToClipboard(ActionEvent actionEvent){
        JMenuItem menuItem = (JMenuItem) actionEvent.getSource();
        var invocation = menuItem.getClientProperty("wFuzzCommand");
        String command = createWfuzzCommand((IContextMenuInvocation) invocation);
        var clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        clip.setContents(new StringSelection(command), null);
    }
}
