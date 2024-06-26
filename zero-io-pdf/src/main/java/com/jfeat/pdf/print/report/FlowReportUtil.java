package com.jfeat.pdf.print.report;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.jfeat.pdf.print.base.ColorDefinition;
import com.jfeat.pdf.print.element.ImageBox;
import com.jfeat.pdf.print.report.request.RowLayoutRequest;
import com.jfeat.pdf.print.report.reports.HeaderFlowReport;
import com.jfeat.pdf.print.report.reports.HeaderFlowReportBuilder;
import com.jfeat.pdf.print.report.request.*;
import com.jfeat.pdf.print.report.row.ImageTextBoxData;
import com.jfeat.pdf.print.util.PageUtil;
import com.jfeat.pdf.print.util.PdfDocumentUtil;
import com.jfeat.pdf.print.util.ElementDrawUtil;

import java.io.*;

/**
 * Created by vincenthuang on 22/03/2018.
 */
public class FlowReportUtil {

    /**
     * PDF模板文件, 输出内容合并到模板上面
     */
    String template;

    public FlowReportUtil template(String templateFile) {
        this.template = templateFile;
        return this;
    }

    /**
     * 通过 FlowReportRequest 构建导出数据
     */
    HeaderFlowReport flowReport;

    /**
     * 通过请求数据转换为打印数据类
     *
     * @param request
     * @return
     */
    public FlowReportUtil data(FlowReportRequest request) {

        RowLayoutRequest headerLayout = request.getLayout().getHeaderLayout();
        RowFormatRequest headerFormat = request.getFormat().getHeaderFormat();
        RowLayoutRequest rowsLayout = request.getLayout().getRowsLayout();
        RowFormatRequest rowsFormat = request.getFormat().getRowsFormat();

//        /// get alignment
//        int headerTitleAlignment = Element.ALIGN_BASELINE;
//        int rowsTitleAlignment = Element.ALIGN_BASELINE;
//        {
//            String LEFT = new Definitions().getTitleAlignments()[1];
//
//            if (headerFormat == null || LEFT.compareTo(headerFormat.getAlignment()) == 0) {
//                headerTitleAlignment = Element.ALIGN_LEFT;
//            }
//
//            if (rowsFormat == null || LEFT.compareTo(rowsFormat.getAlignment()) == 0) {
//                rowsTitleAlignment = Element.ALIGN_LEFT;
//            }
//        }

        HeaderFlowReport report = new HeaderFlowReportBuilder()
                // report
                .columns(request.getColumns())
                .rowRatio(request.getRowRatio())
                .pageMargin(request.getPageMarginLeft(), request.getPageMarginTop(), request.getPageMarginRight(), request.getPageMarginBottom())
                .flowDirection(FlowReportRequest.getFlowDirection(request.getFlowDirection()))
                .rowOption(request.getRowOption())
//                .rowHeight(request.getRowHeight())
                .rowsPadding(request.getRowsPaddingLeft(), request.getRowsPaddingTop(), request.getRowsPaddingRight(), request.getRowsPaddingBottom())
                .borderWidth(rowsLayout.getBorderLeft(), rowsLayout.getBorderRight(), rowsLayout.getBorderTop(), rowsLayout.getBorderBottom())
                .borderColor(rowsLayout.getBorderColor())
                .headerHeight(headerLayout.getHeight())
                .rowHeight(rowsLayout.getHeight())
                .rowAlignment(rowsLayout.getAlignment())
                // header format
                .headerFont(headerFormat.getFont())
                .headerSpacing(headerFormat.getSpacing(), headerFormat.getIndent(), headerFormat.getAlignment(), headerFormat.getVerticalAlignment())
                // header layout
                .headerPadding(headerLayout.getPaddingLeft(), headerLayout.getPaddingRight(), headerLayout.getPaddingTop(), headerLayout.getPaddingBottom())
                .headerBorderWidth(headerLayout.getBorderLeft(), headerLayout.getBorderRight(), headerLayout.getBorderTop(), headerLayout.getBorderBottom())
                .headerBorderColor(headerLayout.getBorderColor())
                // row format
                .rowFont(headerFormat.getFont())
                .rowSpacing(headerFormat.getSpacing(), headerFormat.getIndent(), headerFormat.getAlignment(), headerFormat.getVerticalAlignment())
                // row layout
                .rowPadding(rowsLayout.getPaddingLeft(), rowsLayout.getPaddingRight(), rowsLayout.getPaddingTop(), rowsLayout.getPaddingBottom())
                .rowBorderWidth(rowsLayout.getBorderLeft(), rowsLayout.getBorderRight(), rowsLayout.getBorderTop(), rowsLayout.getBorderBottom())
                .rowBorderColor(rowsLayout.getBorderColor())
                // data
                .headerData(request.getHeaderData())
                .rowsData(request.getRowsData())
                .build();
        this.flowReport = report;

        return this;
    }


    public void export(OutputStream outputStream, float marginLeft, float marginRight, float marginTop, float marginBottom) throws IOException, DocumentException {

        /// offset border
//        if (flowReport.getBorderWidth() > 0) {
//            float offset = flowReport.getBorderWidth() * 0.5f;
//            marginLeft += offset;
//            marginRight += offset;
//        }

        try {
            PdfDocumentUtil.writeDocument(new PdfDocumentUtil.PdfWriteListener() {
                @Override
                public void onDraw(Document document, PdfContentByte canvas) throws DocumentException {

                    if (document != null) {
                        Rectangle pageSize = document.getPageSize();
                        Rectangle contentSize = PageUtil.getContentSize(pageSize, marginLeft, marginRight, marginTop, marginBottom);
                        flowReport.setFlowHeight(contentSize.getHeight());
                    }

                    if (false) {
//                        document.add(new Phrase("Hello world"));
                        PdfPTable table = new PdfPTable(2);
                        table.setWidthPercentage(100);
                        table.addCell("Column 1");
                        table.addCell("Column 2");
                        table.setComplete(true);
                        document.add(table);

                    } else {

                        if (flowReport.getHeader() != null || (flowReport.getRows() != null && flowReport.getRows().size() > 0)) {
                            flowReport.draw(canvas);
                        } else {
                            // no content, just write text
                            ElementDrawUtil.drawText(canvas, "No content");
                        }
                    }

                }
            }, outputStream, template, marginLeft, marginRight, marginTop, marginBottom);

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void export(OutputStream outputStream) throws IOException, DocumentException {
        this.export(outputStream, 0);
    }

    public void export(OutputStream outputStream, float margin) throws IOException, DocumentException {
        this.export(outputStream, margin, margin, margin, margin);
    }

    @Deprecated
    public void export(String pdfFilePath) throws IOException, DocumentException {
        this.export(pdfFilePath, 0);
    }

    @Deprecated
    public void export(String pdfFilePath, float margin) throws IOException, DocumentException {
        this.export(pdfFilePath, margin, margin, margin, margin);
    }

    @Deprecated
    public void export(String filePath, float marginLeft, float marginRight, float marginTop, float marginBottom) throws IOException, DocumentException {
        export(new FileOutputStream(filePath), marginLeft, marginRight, marginTop, marginBottom);
    }

    public static void TestDrawImage(String[] args) throws Exception {
        try {
            PdfDocumentUtil.writeDocument(new PdfDocumentUtil.PdfWriteListener() {
                @Override
                public void onDraw(Document document, PdfContentByte canvas) {

                    Rectangle rect = new Rectangle(20, 20, 200, 200);

                    ElementDrawUtil.drawImage(canvas, "./images/IMG_20220402_143412.jpg",
                            rect, Element.ALIGN_LEFT, 20, 20, 20, 20);
                    rect.setTop(rect.getTop() + 250);
                }

            }, new FileOutputStream("test.pdf"), 0, 0, 0, 0);

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void TestDrawImageList(String[] args) throws Exception {
        FlowReportRequest request = new FlowReportRequest()
                .setColumns(3)
                .setPageMargin(10, 10, 20, 0)
                .setRowsPadding(0.5f, 0.5f, 1.0f, 1.0f)
                .setRowRatio(0.75f)
                .setBorderWidth(0.0f)
                .setRowAlignment(Element.ALIGN_CENTER)
                .setRowOption(ImageBox.ID);

        java.util.List<ImageTextBoxData> imageRows = FlowReportRequest.initImageRowsData("./images");
        request.setRowsData(imageRows);

        new FlowReportUtil()
                .data(request)
                .export(new FileOutputStream("test.pdf"), request.getPageMarginLeft(), request.getPageMarginRight(), request.getPageMarginTop(), 0);
    }

    public static void main(String[] args) throws Exception {
//        float ratio = ImageUtil.getImageRatio("./images/IMG_20220402_143430.jpg");
//        System.out.println(String.format("ratio=%f", ratio));

//        TestDrawImage(args);
        TestDrawImageList(args);
    }
}
