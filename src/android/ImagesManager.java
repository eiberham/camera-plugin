package com.example.acedeno.customcamera;

import android.os.Environment;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

public class ImagesManager {

    public ArrayList<String> images;
    private static final String NOSCONECTA_FOLDERS = "/NosConecta/Photos/";

    ImagesManager(ArrayList<String> images){
        this.images = images;
    }

    /**
     * createPdf
     * Generates a pdf file with the desired images
     * @return generated pdf file path or null in case of no images found
     * @throws FileNotFoundException
     * @throws DocumentException
     */
    protected String createPdf() throws FileNotFoundException, DocumentException {

        if(this.images.size() < 1) return null;
        SecureRandom sRand = new SecureRandom();
        String filename = new BigInteger(130, sRand).toString(32) + ".pdf";
        String target_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                NOSCONECTA_FOLDERS + filename;

        File myFile = new File(target_path);

        OutputStream output = new FileOutputStream(myFile);

        Document document = new Document();

        try {
            PdfWriter.getInstance(document, output);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        document.open();

        for(int i = 0; i <= this.images.size() -1; i ++){

            try {
                Image image = Image.getInstance(this.images.get(i));
                image.setAlignment(Image.ALIGN_CENTER);
                image.scaleToFit((PageSize.A4.getWidth()), (PageSize.A4.getHeight()));
                document.add(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
            document.newPage();
        }

        document.close();

        return target_path;
    }


}
