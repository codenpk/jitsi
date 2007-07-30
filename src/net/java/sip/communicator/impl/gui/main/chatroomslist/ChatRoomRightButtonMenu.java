/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package net.java.sip.communicator.impl.gui.main.chatroomslist;

import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.customcontrols.*;
import net.java.sip.communicator.impl.gui.i18n.*;
import net.java.sip.communicator.impl.gui.main.*;
import net.java.sip.communicator.impl.gui.main.chat.*;
import net.java.sip.communicator.impl.gui.main.chat.conference.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;


/**
 * The <tt>ChatRoomsListRightButtonMenu</tt> is the menu, opened when user clicks
 * with the right mouse button on the chat rooms list panel. It's the one that
 * contains the create chat room item.
 *
 * @author Yana Stamcheva
 */
public class ChatRoomRightButtonMenu
    extends JPopupMenu
    implements  ActionListener
{
    private Logger logger = Logger.getLogger(ChatRoomRightButtonMenu.class);
    
    private I18NString joinChatRoomString
        = Messages.getI18NString("join");
    
    private I18NString leaveChatRoomString
        = Messages.getI18NString("leave");

    private I18NString removeChatRoomString
        = Messages.getI18NString("remove");

    private JMenuItem leaveChatRoomItem = new JMenuItem(
        leaveChatRoomString.getText());
    
    private JMenuItem joinChatRoomItem = new JMenuItem(
        joinChatRoomString.getText());
    
    private JMenuItem removeChatRoomItem = new JMenuItem(
        removeChatRoomString.getText());

    private MainFrame mainFrame;
    
    private ChatRoomWrapper chatRoomWrapper = null;
        
    /**
     * Creates an instance of <tt>ChatRoomsListRightButtonMenu</tt>.
     */
    public ChatRoomRightButtonMenu(MainFrame mainFrame,
        ChatRoomWrapper chatRoomWrapper)
    {
        super();

        this.mainFrame = mainFrame;
        
        this.chatRoomWrapper = chatRoomWrapper;
        
        this.setLocation(getLocation());

        this.init();
    }

    /**
     * Initializes the menu, by adding all containing menu items.
     */
    private void init()
    {
        this.add(joinChatRoomItem);
        this.add(leaveChatRoomItem);
        this.add(removeChatRoomItem);
        
        this.joinChatRoomItem.setName("joinChatRoom");
        this.leaveChatRoomItem.setName("leaveChatRoom");
        this.removeChatRoomItem.setName("removeChatRoom");
        
        this.joinChatRoomItem
            .setMnemonic(joinChatRoomString.getMnemonic());
        
        this.leaveChatRoomItem
            .setMnemonic(leaveChatRoomString.getMnemonic());
    
        this.removeChatRoomItem
            .setMnemonic(removeChatRoomString.getMnemonic());
    
        this.joinChatRoomItem.addActionListener(this);
        this.leaveChatRoomItem.addActionListener(this);
        this.removeChatRoomItem.addActionListener(this);        
    }

    /**
     * Handles the <tt>ActionEvent</tt>. Determines which menu item was
     * selected and makes the appropriate operations.
     */
    public void actionPerformed(ActionEvent e)
    {
        JMenuItem menuItem = (JMenuItem) e.getSource();
        String itemName = menuItem.getName();

        final ChatRoom chatRoom = chatRoomWrapper.getChatRoom();    
        
        if (itemName.equals("removeChatRoom"))
        {            
            ChatWindowManager chatWindowManager
                = mainFrame.getChatWindowManager();
            
            ChatPanel chatPanel
                = chatWindowManager.getMultiChat(chatRoomWrapper);
            
            chatWindowManager.closeChat(chatPanel);
            
            mainFrame.getChatRoomsListPanel()
                .getChatRoomsList().removeChatRoom(chatRoomWrapper);

            if(chatRoom == null)
                return;

            new Thread()
            {
                public void run()
                {
                    chatRoom.leave();
                }
            }.start();
            
        }
        else if (itemName.equals("leaveChatRoom"))
        {
            if(chatRoom == null)
            {
                new ErrorDialog(mainFrame,
                    Messages.getI18NString("haveToBeConnectedToLeave")
                            .getText(),
                    Messages.getI18NString("warning").getText())
                        .showDialog();
                
                return;
            }
            
            new Thread()
            {
                public void run()
                {
                    chatRoom.leave();

                    ChatWindowManager chatWindowManager
                        = mainFrame.getChatWindowManager();

                    ChatPanel chatPanel
                        = chatWindowManager.getMultiChat(chatRoomWrapper);

                    chatWindowManager.closeChat(chatPanel);

                    // Need to refresh the chat room's list in order to change
                    // the state of the chat room to offline.
                    mainFrame.getChatRoomsListPanel()
                        .getChatRoomsList().refresh();
                }
            }.start();
        }
        else if (itemName.equals("joinChatRoom"))
        {
            if(chatRoom == null)
            {
                new ErrorDialog(mainFrame,
                    Messages.getI18NString("haveToBeConnectedToJoin")
                            .getText(),
                    Messages.getI18NString("warning").getText())
                        .showDialog();
                
                return;
            }

            new Thread()
            {
                public void run()
                {
                    try
                    {
                        chatRoom.join();                        
                    }
                    catch (OperationFailedException e)
                    {
                        if(e.getErrorCode()
                            == OperationFailedException.PROVIDER_NOT_REGISTERED)
                        {
                            new ErrorDialog(mainFrame,
                                Messages.getI18NString("chatRoomNotConnected",
                                    new String[]{chatRoom.getName()})
                                        .getText(),
                                Messages.getI18NString("error").getText())
                                    .showDialog();
                        }
                        else if(e.getErrorCode()
                            == OperationFailedException
                                .SUBSCRIPTION_ALREADY_EXISTS)
                        {
                            new ErrorDialog(mainFrame,
                                Messages.getI18NString("chatRoomAlreadyJoined",
                                    new String[]{chatRoom.getName()})
                                        .getText(),
                                Messages.getI18NString("error").getText())
                                    .showDialog();
                        }
                        else
                        {
                            new ErrorDialog(mainFrame,
                                Messages.getI18NString("failedToJoinChatRoom",
                                    new String[]{chatRoom.getName()})
                                        .getText(),
                                Messages.getI18NString("error").getText())
                                    .showDialog();
                        }
                        
                        logger.error("Failed to join chat room: "
                            + chatRoom.getName(), e);
                    }
                }
            }.start();
        }
    }
}
