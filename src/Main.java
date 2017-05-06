import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.FilteredTextRenderListener;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.RegionTextRenderFilter;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpProcessor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, DocumentException {

        PdfReader lecteurPdf = new PdfReader("atdnotif.pdf");
        Rectangle rect1 = new Rectangle( 30, 842-92,170,842-44);
        Rectangle rect2 = new Rectangle(270f,662f,500f,742f);
        RegionTextRenderFilter filter1 = new RegionTextRenderFilter(rect1);
        RegionTextRenderFilter filter2 = new RegionTextRenderFilter(rect2);
        PdfStamper stamper = new PdfStamper(lecteurPdf, new FileOutputStream("coupe.pdf"));
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
        BaseFont bf = BaseFont.createFont("/Library/Fonts/arial.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
        Font arial6 = new Font(bf, 6);
        Font arial8 = new Font(bf, 8);
        bf = BaseFont.createFont("/Library/Fonts/OCR-B10BT.TTF", BaseFont.WINANSI, BaseFont.EMBEDDED);
        Font ocr10 = new Font(bf, 10);
        for (int i = 1; i <= lecteurPdf.getNumberOfPages(); i++) {
            String texte = PdfTextExtractor.getTextFromPage(lecteurPdf, i);
            if((texte.contains("DIRECTION GENERALE DES FINANCES PUBLIQUES") &&
                    (texte.contains("N° 3738") || texte.contains("N° 3735")))) {
                //récupérer l'adresse du SIE
                FilteredTextRenderListener strategy = new FilteredTextRenderListener(new LocationTextExtractionStrategy(), filter1);
                String[] texte1 = PdfTextExtractor.getTextFromPage(lecteurPdf, i, strategy).split("\n");
                texte1[1] += " - recouvrement";
                //récupérer l'adresse du destinataire
                strategy = new FilteredTextRenderListener(new LocationTextExtractionStrategy(), filter2);
                String[] texte2 = PdfTextExtractor.getTextFromPage(lecteurPdf, i, strategy).split("\n");
                System.out.println(texte1 + "\n" + texte2);
                //effacer l'adresse destinataire
                cleanUpLocations.add(new PdfCleanUpLocation(i,rect2));
                PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations,stamper);
                cleaner.cleanUp();
                cleanUpLocations.clear();
                //replacer l'adresse SIE
                PdfContentByte canvas = stamper.getOverContent(i);
                Float y = 724f;
                for (String ligne: texte1) {
                    if(!ligne.startsWith("CS ")) {
                        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                            new Phrase(ligne, arial8),300f, y, 0);
                        y -= 10;
                    }
                }
                //replacer l'adresse destinataire
                y = 645f;
                for (String ligne: texte2) {
                    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                            new Phrase(ligne, ocr10),300f, y, 0);
                    y -= 12;
                }
                //mettre les trois dièses
                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                        new Phrase("###", arial6),28.4f, 28.76f, 0);
            }
        }
        stamper.close();
        lecteurPdf.close();
    }
}
