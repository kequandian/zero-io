package com.jfeat.pdf.print.report.request;

import com.jfeat.pdf.print.base.ColorDefinition;
import com.jfeat.pdf.print.base.ListRowBase;
import com.jfeat.pdf.print.element.ImageTextBox;
import com.jfeat.pdf.print.report.row.ImageTextBoxData;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vincenthuang on 22/03/2018.
 */
public class FlowReportRequest {

    public static final String LTR = "LTR";  // 从左至右
    public static final String UTD = "UTD";  // 从上至下

    private int columns;
    private String flowDirection = LTR;   // LTR, UTD
    private String rowOption = ImageTextBox.ID;   /// select different row layout style {FlowListRow,StackFlowListRow}}
    private float rowRatio = 1.0f;
    private PageSizeRequest pageSize;
    private float pageMarginLeft;
    private float pageMarginTop;
    private float pageMarginRight;
    private float pageMarginBottom;

    public int getColumns() {
        return columns;
    }

    public FlowReportRequest setColumns(int columns) {
        this.columns = columns;
        return this;
    }

    public String getFlowDirection() {
        return flowDirection;
    }

    public FlowReportRequest setFlowDirection(String flowDirection) {
        this.flowDirection = flowDirection;
        return this;
    }

    public String getRowOption() {
        return rowOption;
    }

    public FlowReportRequest setRowOption(String rowOption) {
        this.rowOption = rowOption;
        return this;
    }

    public FlowReportRequest setRowRatio(float ratio) {
        this.rowRatio = ratio;
        return this;
    }

    public float getRowRatio() {
        return this.rowRatio;
    }

    public FlowReportRequest setPageMargin(float margin) {
        this.pageMarginLeft =
                this.pageMarginRight =
                        this.pageMarginTop =
                                this.pageMarginBottom = margin;
        return this;
    }

    public FlowReportRequest setPageMargin(float l, float r, float t, float b) {
        this.pageMarginLeft = l;
        this.pageMarginRight = r;
        this.pageMarginTop = t;
        this.pageMarginBottom = b;
        return this;
    }

    public FlowReportRequest setBorderWidth(float width) {
        this.layout.getRowsLayout().setBorderWidth(width);
        return this;
    }

    public void setBorderColor(int r, int g, int b) {
        this.layout.getRowsLayout().setBorderColor(r, g, b);
    }

    public void setBorderColor(ColorDefinition color) {
        this.layout.getRowsLayout().setBorderColor(color);
    }

    public FlowReportRequest setRowsPadding(float margin) {
        this.layout.rowsLayout.setPadding(margin);
        return this;
    }

    public FlowReportRequest setRowsPadding(float left, float right, float top, float bottom) {
        this.layout.rowsLayout.setPadding(left, right, top, bottom);
        return this;
    }

    public String getRowsMargin() {
        return String.format("%f,%f,%f,%f",
                getRowsPaddingLeft(),
                getRowsPaddingRight(),
                getRowsPaddingTop(),
                getRowsPaddingBottom());
    }

    public void setRowsPadding(String margin) {
        try {
            if (margin != null && margin.contains(",")) {
                String[] margins = margin.split(",");
                if (margins.length == 4) {
                    float left = Float.parseFloat(margins[0]);
                    float right = Float.parseFloat(margins[1]);
                    float top = Float.parseFloat(margins[2]);
                    float bottom = Float.parseFloat(margins[3]);
                    setRowsPadding(left, right, top, bottom);
                }
            } else {
                float marginF = Float.parseFloat(margin);
                setRowsPadding(marginF);
            }
        } catch (NumberFormatException e) {
        }
    }

    /**
     * data
     */
    private ListRowBase headerData;

    private List<ListRowBase> rowsData;

    public ListRowBase getHeaderData() {
        return headerData;
    }

    public FlowReportRequest setHeaderData(ListRowBase headerData) {
        this.headerData = headerData;
        return this;
    }

    public List<ListRowBase> getRowsData() {
        return rowsData;
    }

    public FlowReportRequest setRowsData(List<ImageTextBoxData> rows) {
        if (flowDirection == LTR) {
            if (rows.size() % 2 == 1) {
                rows.add(ImageTextBoxData.EMPTY);
            }
        }
        this.rowsData = new ArrayList<>();
        this.rowsData.addAll(rows);

        for (ImageTextBoxData it : rows) {
            it.setPaddingLeft(getRowsPaddingLeft());
            it.setPaddingRight(getRowsPaddingRight());
            it.setPaddingTop(getRowsPaddingTop());
            it.setPaddingBottom(getRowsPaddingBottom());
        }

        return this;
    }


    /**
     * 初始化图像目录数据
     *
     * @param imageDir
     * @return
     */
    public static List<ImageTextBoxData> initImageRowsData(String imageDir) {
//        if(this.rowsData==null) {
//            this.rowsData = new ArrayList<T>();
//        }
        List<ImageTextBoxData> rowsData = new ArrayList<>();

        File dirRoot = new File(imageDir);
        Assert.isTrue(dirRoot.exists() && dirRoot.isDirectory(), dirRoot + " not exists!");

        String[] imageUrls = dirRoot.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".png") ||
                        name.endsWith(".jpeg") ||
                        name.endsWith(".jpg");
            }
        });

        for (String url : imageUrls) {

            ImageTextBoxData rowData = new ImageTextBoxData();
            rowData.setImageUrl(String.join(File.separator, imageDir, url));
            rowData.setTitle(FilenameUtils.getName(url));

            rowsData.add(rowData);
        }

        return rowsData;
    }


    /**
     * 定义表头与行打印格式
     * format
     */
    private FormatRequest format = new FormatRequest();

    public FormatRequest getFormat() {
        return format;
    }

    public FlowReportRequest setFormat(FormatRequest format) {
        this.format = format;
        return this;
    }

    /**
     * 定义表头与行的打印布局
     * layout
     */
    private LayoutRequest layout = new LayoutRequest();

    public LayoutRequest getLayout() {
        return layout;
    }

    public FlowReportRequest setLayout(LayoutRequest layout) {
        this.layout = layout;
        return this;
    }

    public float getRowHeight() {
        return this.layout.getRowsLayout().getHeight();
    }

    public FlowReportRequest setRowHeight(float height) {
        this.layout.rowsLayout.setHeight(height);
        return this;
    }

    public FlowReportRequest setRowAlignment(int alignment) {
        this.layout.rowsLayout.setAlignment(alignment);
        return this;
    }

    public float getRowsPaddingLeft() {
        return this.layout.rowsLayout.getPaddingLeft();
    }

    public float getRowsPaddingRight() {
        return this.layout.rowsLayout.getPaddingRight();
    }

    public float getRowsPaddingTop() {
        return this.layout.rowsLayout.getPaddingTop();
    }

    public float getRowsPaddingBottom() {
        return this.layout.rowsLayout.getPaddingBottom();
    }


    /**
     * 数据定义，用于参考
     *
     * @return
     */
    private Definitions definitions = new Definitions();

    public Definitions getDefinitions() {
        return definitions;
    }

    public static int getFlowDirection(String flowDirection) {
        return flowDirection.equals(FlowReportRequest.LTR) ? 0 :
                (flowDirection.equals(FlowReportRequest.UTD) ? 1 : -1);
    }

    public float getPageMarginLeft() {
        return pageMarginLeft;
    }

    public void setPageMarginLeft(float pageMarginLeft) {
        this.pageMarginLeft = pageMarginLeft;
    }

    public float getPageMarginTop() {
        return pageMarginTop;
    }

    public void setPageMarginTop(float pageMarginTop) {
        this.pageMarginTop = pageMarginTop;
    }

    public float getPageMarginRight() {
        return pageMarginRight;
    }

    public void setPageMarginRight(float pageMarginRight) {
        this.pageMarginRight = pageMarginRight;
    }

    public float getPageMarginBottom() {
        return pageMarginBottom;
    }

    public void setPageMarginBottom(float pageMarginBottom) {
        this.pageMarginBottom = pageMarginBottom;
    }

    public PageSizeRequest getPageSize() {
        return pageSize;
    }

    public void setPageSize(PageSizeRequest pageSize) {
        this.pageSize = pageSize;
    }

    public void setPageSize(float w, float h) {
        this.pageSize.width = w;
        this.pageSize.height = h;
    }

    /**
     * format request
     */
    public static class FormatRequest {
        private RowFormatRequest headerFormat = new RowFormatRequest();
        private RowFormatRequest rowsFormat = new RowFormatRequest();
        private RowFormatRequest groupsFormat = new RowFormatRequest();

        public RowFormatRequest getHeaderFormat() {
            return headerFormat;
        }

        public void setHeaderFormat(RowFormatRequest headerFormat) {
            this.headerFormat = headerFormat;
        }

        public RowFormatRequest getRowsFormat() {
            return rowsFormat;
        }

        public void setRowsFormat(RowFormatRequest rowsFormat) {
            this.rowsFormat = rowsFormat;
        }

        public RowFormatRequest getGroupsFormat() {
            return groupsFormat;
        }
    }


    /**
     * 定义表头与行布局
     * layout request
     */
    public static class LayoutRequest {

        private RowLayoutRequest headerLayout = new RowLayoutRequest();
        private RowLayoutRequest rowsLayout = new RowLayoutRequest();

        public RowLayoutRequest getHeaderLayout() {
            return headerLayout;
        }

        public void setHeaderLayout(RowLayoutRequest headerLayout) {
            this.headerLayout = headerLayout;
        }

        public RowLayoutRequest getRowsLayout() {
            return rowsLayout;
        }

        public void setRowsLayout(RowLayoutRequest rowsLayout) {
            this.rowsLayout = rowsLayout;
        }
    }

    public static class PageSizeRequest {
        private float width;
        private float height;

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }
    }
}
