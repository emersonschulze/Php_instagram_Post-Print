package classes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.imgscalr.Scalr;
import org.json.JSONObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Photo {

    public String id = "";
    public String url = "";
    public String nome_usuario = "";
    public String foto_usuario = "";
    public String description = "";

    public File fotoFromTemplate;

    private int papel_height = 803;
    private int papel_width = 598;

    private int foto_width = 540;
    private int foto_height = 540;
    private int foto_left = 0;
    private int foto_top = 0;

    private int usuario_top = 0;
    private int usuario_left = 0;
    private String usuario_font = "Times New Roman";
    private int usuario_font_size = 0;
    private String usuario_font_style = "NORMAL";
    private String usuario_align = "NORMAL";
    private int usuario_foto_left = 0;
    private int usuario_foto_top = 0;
    private static int usuario_foto_height = 55;
    private static int usuario_foto_width = 55;

    private int empresa_width = 0;
    private int empresa_height = 0;
    private int empresa_left = 0;
    private int empresa_top = 0;
    private String empresa_image = "";

    private int evento_width = 0;
    private int evento_height = 0;
    private int evento_left = 0;
    private int evento_top = 0;
    private String evento_font = "Times New Roman";
    private int evento_font_size = 0;
    private String evento_font_style = "NORMAL";
    private String evento_align = "NORMAL";

    public String nome_evento = "";
    public String image_evento = "";

    private Webservice ws;

    public Photo() {
        ws = new Webservice();
    }

    @SuppressWarnings("empty-statement")
    private void setup(JSONObject json, double perc) {
        if (perc == 0) {
            perc = 1;
        }
        papel_width = (int) Math.floor(json.getDouble("papel_width") * perc);
        papel_height = (int) Math.floor(json.getDouble("papel_height") * perc);

        foto_width = (int) Math.floor(json.getDouble("foto_width") * perc);
        foto_height = (int) Math.floor(json.getDouble("foto_height") * perc);
        foto_left = (int) Math.floor(json.getDouble("foto_left") * perc);
        foto_top = (int) Math.floor(json.getDouble("foto_top") * perc);

        usuario_foto_height = (int) (55 * perc);
        usuario_foto_width = (int) (55 * perc);
        usuario_top = (int) Math.floor((json.getDouble("usuario_top") + 15) * perc);
        usuario_left = (int) Math.floor((json.getDouble("usuario_left") + 65) * perc);
        usuario_foto_top = (int) Math.floor((json.getDouble("usuario_top") - 15) * perc);
        usuario_foto_left = (int) Math.floor(json.getDouble("usuario_left") * perc);

        usuario_font = json.getString("usuario_font");
        usuario_font_size = (int) Math.floor(json.getDouble("usuario_font_size") * perc);
        usuario_font_style = json.getString("usuario_font_style");
        usuario_align = json.getString("usuario_align");

        empresa_width = (int) Math.floor(json.getDouble("empresa_width") * perc);
        empresa_height = (int) Math.floor(json.getDouble("empresa_height") * perc);
        empresa_left = (int) Math.floor(json.getDouble("empresa_left") * perc);
        empresa_top = (int) Math.floor(json.getDouble("empresa_top") * perc);
        empresa_image = json.getString("empresa_image");

        if (json.getString("evento_width").equals("") == false) {
            evento_width = (int) Math.floor(json.getDouble("evento_width") * perc);
        }
        if (json.getString("evento_height").equals("") == false) {
            evento_height = (int) Math.floor(json.getDouble("evento_height") * perc);
        }
        if (json.getString("evento_left").equals("") == false) {
            evento_left = (int) Math.floor(json.getDouble("evento_left") * perc);
        }

        evento_top = (int) Math.floor(json.getDouble("evento_top") * perc);
        evento_font = json.getString("evento_font");
        evento_font_size = (int) Math.floor(json.getDouble("evento_font_size") * perc);
        evento_font_style = json.getString("evento_font_style");
        evento_align = json.getString("evento_align");

    }

    private void printCenterString(Graphics g2d, String s, int width, int XPos, int YPos) {
        int stringLen = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();
        int start = width / 2 - stringLen / 2;
        g2d.drawString(s, start + XPos, YPos);
    }

    private void printCenterImage(Graphics g2d, Image image, int width, int widthImage, int heightImage, int XPos, int YPos) {
        int start = width / 2 - widthImage / 2;
        g2d.drawImage(image, start + XPos, YPos, null);
    }

    private void printCenterLabel(JLabel label, Image image, int width, int widthImage, int heightImage, int XPos, int YPos) {
        int start = width / 2 - widthImage / 2;
        label.setLocation(start + XPos, YPos);
        label.setIcon(new ImageIcon(image));
    }

    private void printCenterStringLabel(JLabel label, String s, int width, int XPos, int YPos) {
        int stringLen = (int) label.getGraphics().getFontMetrics().getStringBounds(s, label.getGraphics()).getWidth();
        int start = width / 2 - stringLen / 2;
        label.setText(s);
        label.setLocation(start + XPos, YPos);
    }

    public int getWidthImage(Integer origWidth, Integer origHeight, Integer width, Integer height) {
        if (width == 0) {
            width = origWidth;
        }
        if (height == 0) {
            height = origHeight;
        }

        int fHeight = height;
        int fWidth = width;

        //Work out the resized width/height
        if (origHeight > height || origWidth > width) {
            fHeight = height;
            int wid = width;
            float sum = (float) origWidth / (float) origHeight;
            fWidth = Math.round(fHeight * sum);

            if (fWidth > wid) {
                //rezise again for the width this time
                fHeight = Math.round(wid / sum);
                fWidth = wid;
            }
        }

        return fWidth;
    }

    public boolean createImageFromTemplate(File arquivo, String dirOutPut, String id_template) {

        File path = new File(dirOutPut);

        try {

            JSONObject json = ws.getConfigTemplate(id_template);
            if (json != null) {
                setup(json, 1);

                //Graphics2D  graphics = background.createGraphics();
                //graphics.setPaint ( new Color ( 0, 0, 0) );
                //graphics.fillRect ( 0, 0, background.getWidth(), background.getHeight() );
                BufferedImage foto = ImageIO.read(new File(arquivo.getAbsolutePath()));

                BufferedImage combined = new BufferedImage(papel_width, papel_height, BufferedImage.TYPE_INT_RGB);
                combined.setRGB(255, 255, 255);

                // paint both images, preserving the alpha channels
                Graphics2D g = combined.createGraphics();
                //g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                g.setBackground(Color.WHITE);
                g.fillRect(0, 0, papel_width, papel_height);
                g.setColor(Color.BLACK);

                foto = Scalr.resize(foto, foto_width, foto_height, Scalr.OP_ANTIALIAS);

                g.drawImage(foto, foto_left, foto_top, null);

                if (empresa_image.equals("") == false && empresa_image != null && empresa_image.equals("null") == false) {
                    BufferedImage empresa_logo = ImageIO.read(new URL(empresa_image));
                    empresa_logo = Scalr.resize(empresa_logo, empresa_width, empresa_height, Scalr.OP_ANTIALIAS);
                    g.drawImage(empresa_logo, empresa_left, empresa_top, null);
                }

                if (foto_usuario.equals("") == false && foto_usuario != null && foto_usuario.equals("null") == false) {
                    BufferedImage fotoUser = ImageIO.read(new URL(foto_usuario));
                    fotoUser = Scalr.resize(fotoUser, usuario_foto_width, usuario_foto_height, Scalr.OP_ANTIALIAS);
                    g.drawImage(fotoUser, usuario_foto_left, usuario_foto_top, null);
                }

                Font font = new Font(usuario_font, getStyleFont(usuario_font_style), usuario_font_size);
                g.setFont(font);
                g.drawString(nome_usuario, usuario_left, usuario_top);

                if (image_evento.equals("") == false && image_evento != null && image_evento.equals("null") == false) {

                    File arquivo_evento_logo = new File(image_evento);
                    BufferedImage evento_logo = ImageIO.read(arquivo_evento_logo);
                    int width_evento_logo = getWidthImage(evento_logo.getWidth(), evento_logo.getHeight(), evento_width, evento_height);

                    evento_logo = Scalr.resize(evento_logo, width_evento_logo, evento_height, Scalr.OP_ANTIALIAS);
                    if (evento_align.equals("NORMAL")) {
                        g.drawImage(evento_logo, evento_left, evento_top, null);
                    } else {
                        if (evento_align.equals("CENTER")) {
                            printCenterImage(g, evento_logo, papel_width, width_evento_logo, evento_height, evento_left, evento_top);
                        } else if (evento_align.equals("RIGHT")) {
                            g.drawImage(evento_logo, papel_width - width_evento_logo - foto_left, evento_top, null);
                        } else {
                            g.drawImage(evento_logo, evento_left, evento_top, null);
                        }
                    }
                } else {

                    if (evento_height > 0) {
                        evento_top = evento_top + (int) (evento_height / 1.5f);
                    }

                    Font font2 = new Font(evento_font, getStyleFont(evento_font_style), evento_font_size);
                    g.setFont(font2);
                    if (evento_align.equals("NORMAL")) {
                        g.drawString(nome_evento, evento_left, evento_top);
                    } else {
                        printCenterString(g, nome_evento, papel_width, evento_left, evento_top);
                    }
                }

                g.dispose();

                //System.out.println("Foto usuário: "+foto_usuario);
                fotoFromTemplate = new File(path, arquivo.getName());
                // Save as new image

                //saveBufferedImageAsJPEG(combined, 200, fotoFromTemplate.getAbsolutePath());
                saveImageToDisk(combined, 200, fotoFromTemplate.getAbsolutePath());

                //saveGridImage(combined, 200, fotoFromTemplate);
                /*
                // Encontra o ImageWriter correto de acordo com o sufixo  
                Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpg");  
                
                ImageWriter writer = (ImageWriter) writers.next();  

                // Cria um conjunto de parâmetros para configuração  

                ImageWriteParam param = writer.getDefaultWriteParam();  

                // Altera para não usar compressão automática  

                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);  

                // Muda a taxa de compressão para 100% (valor entre 0 e 1)  

                param.setCompressionQuality(1);  

                // Salva a imagem  
                FileImageOutputStream output = new FileImageOutputStream(fotoFromTemplate);  
                writer.setOutput(output);  
                writer.write(null, new IIOImage(combined, null, null), param);  
                writer.dispose();
                 */
                //ImageIO.write(combined, "PNG", fotoFromTemplate);
                return true;

            } else {
                System.out.println("Não achou a configuração do template");
            }
        } catch (IOException ex) {
            Logger.getLogger(Photo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;

    }

    /*
    public void savejpgimage(PlanarImage inputImage, File file){
        try {
            RenderedImage rendImage = inputImage;
            ImageOutputStream ios = ImageIO.createImageOutputStream(file);
            ImageWriter writer = null;
            Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
            if (iter.hasNext()) {
                writer = (ImageWriter)iter.next();
            }
            ImageTypeSpecifier ss = new ImageTypeSpecifier(inputImage);
            writer.setOutput(ios);
            ImageWriteParam iwparam = writer.getDefaultWriteParam();
            JPEGImageWriteParam iwparam1 = (JPEGImageWriteParam) writer.getDefaultWriteParam();
            iwparam1.setCompressionMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
            IIOMetadata ff ;
            ff = writer.getDefaultImageMetadata(ss,iwparam1);
            writer.write(ff, new IIOImage(rendImage, null, null), iwparam1);
            ios.flush();
            writer.dispose();
            ios.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     */
    public void setAlign(JLabel label, String align) {
        if (align.equals("CENTER")) {
            label.setHorizontalAlignment(JLabel.CENTER);
        } else if (align.equals("RIGHT")) {
            label.setHorizontalAlignment(JLabel.RIGHT);
        } else {
            label.setHorizontalAlignment(JLabel.LEFT);
        }

    }

    public int getStyleFont(String fontStyle) {
        if (fontStyle.equals("BOLD")) {
            return Font.BOLD;
        } else if (fontStyle.equals("ITALIC")) {
            return Font.ITALIC;
        }
        return Font.PLAIN;
    }

    public void updatePreview(String id_template, JLabel foto, JLabel lblTexto1, JLabel lblTexto2, JLabel lblNomeUsuario, JLabel lblFotoUsuario) {

        try {
            System.out.println("Antes Config Template");
            JSONObject json = ws.getConfigTemplate(id_template);
            System.out.println("Carregou Config Template");
            if (json != null) {
                setup(json, 0.518394649f);

                foto.setLocation(foto_left, foto_top);
                //System.out.println("X = "+foto.getX()+" - Y = "+foto.getY());

                Font font = new Font(usuario_font, getStyleFont(usuario_font_style), usuario_font_size);

                lblNomeUsuario.setLocation(usuario_left, usuario_top - 12);
                lblNomeUsuario.setFont(font);
                setAlign(lblNomeUsuario, usuario_align);
                //if(foto_usuario.equals("") == false && foto_usuario != null && foto_usuario.equals("null") == false){
                //BufferedImage fotoUser = ImageIO.read(new URL(foto_usuario));
                //fotoUser = Scalr.resize(fotoUser, usuario_foto_width, usuario_foto_height, Scalr.OP_ANTIALIAS);

                //lblFotoUsuario.setIcon(new ImageIcon(fotoUser));
                //System.out.println("X = "+usuario_foto_left+" - Y = "+usuario_foto_top+" - W = "+usuario_foto_width+" - H = "+usuario_foto_height);
                lblFotoUsuario.setText("");
                lblFotoUsuario.setPreferredSize(new Dimension(usuario_foto_width, usuario_foto_height));
                lblFotoUsuario.setSize(usuario_foto_width, usuario_foto_height);
                lblFotoUsuario.setLocation(usuario_foto_left, usuario_foto_top);
                //}

                if (empresa_image.equals("") == false && empresa_image != null && empresa_image.equals("null") == false) {
                    BufferedImage empresa_logo = ImageIO.read(new URL(empresa_image));
                    empresa_logo = Scalr.resize(empresa_logo, empresa_width, empresa_height, Scalr.OP_ANTIALIAS);
                    lblTexto1.setText("");
                    lblTexto1.setPreferredSize(new Dimension(empresa_width, empresa_height));
                    lblTexto1.setSize(empresa_width, empresa_height);
                    lblTexto1.setLocation(empresa_left, empresa_top);
                    lblTexto1.setIcon(new ImageIcon(empresa_logo));
                }
                if (image_evento != null) {
                    boolean existeFotoEvento = false;
                    if (new File(image_evento).exists()) {
                        existeFotoEvento = true;
                    }

                    if (existeFotoEvento == true && image_evento.equals("") == false && image_evento != null && image_evento.equals("null") == false) {
                        System.out.println("IMAGEM DE LOGO: " + image_evento);
                        File arquivo_evento_logo = new File(image_evento);
                        try {
                            BufferedImage evento_logo = ImageIO.read(arquivo_evento_logo);
                            int width_evento_logo = getWidthImage(evento_logo.getWidth(), evento_logo.getHeight(), evento_width, evento_height);

                            evento_logo = Scalr.resize(evento_logo, width_evento_logo, evento_height, Scalr.OP_ANTIALIAS);
                            lblTexto2.setText("");
                            lblTexto2.setPreferredSize(new Dimension(width_evento_logo, evento_height));
                            lblTexto2.setSize(width_evento_logo, evento_height);

                            //setAlign(lblTexto2, evento_align);
                            if (evento_align.equals("CENTER")) {
                                printCenterLabel(lblTexto2, evento_logo, papel_width, width_evento_logo, evento_height, evento_left, evento_top);
                            } else if (evento_align.equals("RIGHT")) {
                                lblTexto2.setLocation(papel_width - width_evento_logo - foto_left, evento_top);
                                lblTexto2.setIcon(new ImageIcon(evento_logo));
                            } else {
                                lblTexto2.setLocation(evento_left, evento_top);
                                lblTexto2.setIcon(new ImageIcon(evento_logo));
                            }

                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(null, "As configurações desta imagem não são suportadas. Dica: abra esta imagem no paint ou photoshop e salve como PNG");
                        }

                        /*if(evento_align.equals("NORMAL")){
                            lblTexto2.setLocation(evento_left, evento_top);
                            lblTexto2.setIcon(new ImageIcon(evento_logo));
                        }else{
                            printCenterLabel(lblTexto2, evento_logo, papel_width, width_evento_logo ,evento_height, evento_left, evento_top);
                        }*/
                    } else {

                        int width_string = (int) lblTexto2.getGraphics().getFontMetrics().getStringBounds(nome_evento, lblTexto2.getGraphics()).getWidth();

                        if (evento_height > 0) {
                            evento_top = evento_top + (int) (evento_height / 3);
                        }

                        lblTexto2.setIcon(null);
                        lblTexto2.setPreferredSize(new Dimension(width_string, evento_height));
                        lblTexto2.setSize(width_string, evento_height);
                        lblTexto2.setHorizontalAlignment(JLabel.LEFT);

                        Font font2 = new Font(nome_evento, getStyleFont(evento_font_style), evento_font_size);
                        lblTexto2.setFont(font2);

                        //setAlign(lblTexto2, evento_align);
                        if (evento_align.equals("CENTER")) {
                            printCenterStringLabel(lblTexto2, nome_evento, papel_width, evento_left, evento_top);
                        } else if (evento_align.equals("RIGHT")) {
                            System.out.println("Align RIGHT");
                            lblTexto2.setHorizontalAlignment(JLabel.RIGHT);
                            lblTexto2.setLocation(papel_width - width_string - foto_left, evento_top);
                            lblTexto2.setText(nome_evento);
                        } else {
                            lblTexto2.setLocation(evento_left, evento_top);
                            lblTexto2.setText(nome_evento);
                        }

                        /*
                        if(evento_align.equals("NORMAL")){
                            lblTexto2.setText(nome_evento);
                            lblTexto2.setLocation(evento_left, evento_top);
                        }else{
                            printCenterStringLabel(lblTexto2, nome_evento, papel_width, evento_left, evento_top);
                        }*/
                    }
                }

            } else {
                System.out.println("Não achou a configuração do template");
            }
        } catch (Exception ex) {
            Logger.getLogger(Photo.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Setou TUDO");
    }

    /*
    public void saveBufferedImageAsJPEG(BufferedImage bi, int dpi, String fileName)
    {
        // save image as Jpeg
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(fileName);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
            param.setQuality(1f, false);
            param.setDensityUnit(JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
            param.setXDensity(dpi);
            param.setYDensity(dpi);
            encoder.setJPEGEncodeParam(param);
            try
            {
                encoder.encode(bi);
                out.close();
            }
            catch (IOException io)
            {
                System.out.println(io);
            }
        }
        catch (FileNotFoundException fnf)
        {
            System.out.println("File Not Found");
        }
    }*/
    public static void saveGridImage(BufferedImage sourceImage, int DPI,
            File output) throws IOException {
        output.delete();

        final String formatName = "png";

        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);//new ImageTypeSpecifier(sourceImage.getColorModel(), sourceImage.getSampleModel());
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                continue;
            }
            setDPI(metadata, DPI);

            final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
            try {
                writer.setOutput(stream);
                writer.write(metadata, new IIOImage(sourceImage, null, metadata), writeParam);
            } finally {
                stream.close();
            }
            break;
        }
    }

    public static void setDPI(IIOMetadata metadata, int DPI)
            throws IIOInvalidTreeException {

        double INCH_2_CM = 2.54;

        // for PNG, it's dots per millimeter
        double dotsPerMilli = 1.0 * DPI / 10 / INCH_2_CM;

        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);

        metadata.mergeTree("javax_imageio_1.0", root);
    }

    public static void saveImageToDisk(BufferedImage bi, int dpi, String fileName) {
        Iterator i = ImageIO.getImageWritersByFormatName("jpeg");
        //are there any jpeg encoders available?

        if (i.hasNext()) //there's at least one ImageWriter, just use the first one
        {
            ImageWriter imageWriter = (ImageWriter) i.next();
            //get the param
            ImageWriteParam param = imageWriter.getDefaultWriteParam();
            ImageTypeSpecifier its = new ImageTypeSpecifier(bi.getColorModel(), bi.getSampleModel());

            //get metadata
            IIOMetadata iomd = imageWriter.getDefaultImageMetadata(its, param);

            String formatName = "javax_imageio_jpeg_image_1.0";//this is the DOCTYPE of the metadata we need

            Node node = iomd.getAsTree(formatName);
            //what are child nodes?
            NodeList nl = node.getChildNodes();
            for (int j = 0; j < nl.getLength(); j++) {
                Node n = nl.item(j);
                //System.out.println("node from IOMetadata is : " + n.getNodeName());

                if (n.getNodeName().equals("JPEGvariety")) {
                    NodeList childNodes = n.getChildNodes();

                    for (int k = 0; k < childNodes.getLength(); k++) {
                        //System.out.println("node #" + k + " is " + childNodes.item(k).getNodeName());
                        if (childNodes.item(k).getNodeName().equals("app0JFIF")) {
                            NamedNodeMap nnm = childNodes.item(k).getAttributes();
                            //get the resUnits, Xdensity, and Ydensity attribuutes
                            Node resUnitsNode = getAttributeByName(childNodes.item(k), "resUnits");
                            Node XdensityNode = getAttributeByName(childNodes.item(k), "Xdensity");
                            Node YdensityNode = getAttributeByName(childNodes.item(k), "Ydensity");
                            //reset values for nodes
                            resUnitsNode.setNodeValue("1"); //indicate DPI mode
                            XdensityNode.setNodeValue(String.valueOf(dpi));
                            YdensityNode.setNodeValue(String.valueOf(dpi));

                            //System.out.println("name=" +resUnitsNode.getNodeName() + ", value=" + resUnitsNode.getNodeValue());
                            //System.out.println("name=" +XdensityNode.getNodeName() + ", value=" + XdensityNode.getNodeValue());
                            //System.out.println("name=" +YdensityNode.getNodeName() + ", value=" + YdensityNode.getNodeValue());
                        }    //end  if (childNodes.item(k).getNodeName().equals("app0JFIF"))
                    } //end if (n.getNodeName().equals("JPEGvariety")
                    break; //we don't care about the rest of the children
                } //end if (n.getNodeName().equals("JPEGvariety"))

            } //end  for (int j = 0; j < nl.getLength(); j++)

            try {
                iomd.setFromTree(formatName, node);
            } catch (IIOInvalidTreeException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
            //attach the metadata to an image
            IIOImage iioimage = new IIOImage(bi, null, iomd);
            try {
                imageWriter.setOutput(new FileImageOutputStream(new File(fileName)));
                imageWriter.write(iioimage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }  //end if (i.hasNext()) //there's at least one ImageWriter, justuse the first one
    }  //end public static void saveImageToDisk(BufferedImage bi, int dpi,String fileName)

    /**
     * @param node
     * @param attributeName - name of child node to return
     * @return
     */
    static Node getAttributeByName(Node node, String attributeName) {
        if (node == null) {
            return null;
        }
        NamedNodeMap nnm = node.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = nnm.item(i);
            if (n.getNodeName().equals(attributeName)) {
                return n;
            }
        }
        return null; // no such attribute was found
    }

}
