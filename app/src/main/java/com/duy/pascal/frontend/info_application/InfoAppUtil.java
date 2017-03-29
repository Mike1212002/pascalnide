package com.duy.pascal.frontend.info_application;

import com.duy.pascal.frontend.DLog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Duy on 29-Mar-17.
 */

public class InfoAppUtil {

    /*  public static ArrayList<ItemInfo> readListTranslate(Context context, String filePath, boolean fromAssets)
              throws IOException, UnexpectedElementException, SAXException, ParserConfigurationException {
          ArrayList<ItemInfo> result = new ArrayList<>();

          InputStream inputStream;
          if (fromAssets) {
              inputStream = context.getAssets().open(filePath);
          } else {
              inputStream = new FileInputStream(filePath);
          }
          Node root = XmlRead.getRootNode(inputStream);
          Node child = root.getFirstChild();
          while (child != null) {
              NamedNodeMap attr = child.getAttributes();
              Node name = attr.getNamedItem("name");
              Node link = attr.getNamedItem("link");
              Node image = attr.getNamedItem("image");

              String sName = name.getNodeValue();
              String sLink = link.getNodeValue();
              String imgPath = image.getNodeValue();
              result.add(new ItemInfo(sName, sLink, imgPath));
              child = root.getNextSibling();
          }
          return result;
      }
  */
    public static ArrayList<ItemInfo> readListTranslate(InputStream inputStream) {
        ArrayList<ItemInfo> result = new ArrayList<>();
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            Element root = document.getDocumentElement();
            NodeList nodeList = root.getChildNodes();
            DLog.i(nodeList.getLength());
            for (int index = 0; index < nodeList.getLength(); index++) {
                Node node = nodeList.item(index);
                if (node instanceof Element) {
                    String name = ((Element) node).getAttribute("name");
                    String link = ((Element) node).getAttribute("link");
                    String img = ((Element) node).getAttribute("image");
                    String lang = ((Element) node).getAttribute("lang");
                    result.add(new ItemInfo(name, link, img, lang));
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
