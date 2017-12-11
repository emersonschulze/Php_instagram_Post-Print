package photoparty;

import javax.swing.JFrame;

public class PhotoParty {

    public static void main(String[] args) {
        
        //validação se está logado, se sim chama a frmPrincipal
        
        
        FrmPrincipal frm = new FrmPrincipal();
        frm.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frm.setVisible(true);
    }
}
