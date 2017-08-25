/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package photoparty;

import classes.Core;
import javax.swing.JFrame;



/**
 *
 * @author cardial
 */
public class PhotoParty{
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Core core = new Core();
        if(core.existCertificate()){
            if(core.isValidCertificate()){
                FrmPrincipal frm = new FrmPrincipal();
                frm.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frm.setVisible(true);
            }else{
                FrmAssign frmAssign = new FrmAssign();
                frmAssign.step = 2;
                frmAssign.setVisible(true);
            }
        }else{
            FrmAssign frmAssign = new FrmAssign();
            frmAssign.step = 1;
            frmAssign.setVisible(true);
        }
    }
    
}
