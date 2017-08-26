
package photoparty;

import javax.swing.JFrame;
public
 class PhotoParty{
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FrmPrincipal frm = new FrmPrincipal();
        frm.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frm.setVisible(true);
    }
 }
