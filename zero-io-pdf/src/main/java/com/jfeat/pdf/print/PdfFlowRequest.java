package com.jfeat.pdf.print;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.jfeat.pdf.print.base.BorderDefinition;
import com.jfeat.pdf.print.base.FontDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by vincent on 2018/10/8.
 * 流文档请求体
 */
public class PdfFlowRequest {

    private Page page;
    private List<Flow> flows;

    /// 定义
    private Definitions definitions;

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public List<Flow> getFlows() {
        return flows;
    }

    public void setFlows(List<Flow> flows) {
        this.flows = flows;
    }

    public Definitions getDefinitions() {
        return definitions;
    }

    public void setDefinitions(Definitions definitions) {
        this.definitions = definitions;
    }

    /**
     * Pdf导出页面设置，大小，边框
     */
    public static class Page {
        private String pageName;
        private Rectangle pageSize;
        private String imageUrl;

        private float marginLeft;
        private float marginTop;
        private float marginRight;
        private float marginBottom;

        public Page() {
        }

        public Page(String pageName, float margin) {
            this.pageName = pageName;
            this.pageSize = getPageSize(pageName);
            setMargin(margin);
        }

        public Page(String pageName, Boolean rotate, float margin) {
            this.pageName = pageName;
            Rectangle pageSize = getPageSize(pageName);
            this.pageSize = rotate == null ? pageSize : (rotate ? pageSize.rotate() : pageSize);
            setMargin(margin);
        }

        public Page(String pageName, Boolean rotate, String imageUrl, float margin) {
            this.pageName = pageName;
            this.imageUrl = imageUrl;
            Rectangle pageSize = getPageSize(pageName);
            this.pageSize = rotate == null ? pageSize : (rotate ? pageSize.rotate() : pageSize);
            setMargin(margin);
        }

        public Page(String pageName, float marginLeft, float marginTop, float marginRight, float marginBottom) {
            this.pageName = pageName;
            this.pageSize = getPageSize(pageName);
            setMargin(marginLeft, marginTop, marginRight, marginBottom);
        }

        public Page(float pageWidth, float pageHeight, float margin) {
            this.pageSize = new Rectangle(0, 0, pageWidth, pageHeight);
            setMargin(margin);
        }

        public Page(float pageWidth, float pageHeight, float marginLeft, float marginTop, float marginRight, float marginBottom) {
            this.pageSize = new Rectangle(0, 0, pageWidth, pageHeight);
            setMargin(marginLeft, marginTop, marginRight, marginBottom);
        }

        public Rectangle getPageSize(String pageName) {

            if ("A5".equals(pageName)) {
                return PageSize.A5;
            } else if ("A4".equals(pageName)) {
                return PageSize.A4;
            } else if ("A3".equals(pageName)) {
                return PageSize.A3;
            } else {
                throw new RuntimeException("not implement, provide the page if required.");
            }
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getPageName() {
            return pageName;
        }

        public void setPageName(String pageName) {
            if (pageName == null || pageName.length() == 0) {
                throw new RuntimeException("empty page name is not allowed");
            }
            this.pageName = pageName;

            // update page size
            this.pageSize = getPageSize(pageName);
        }

        public void setPageSize(Rectangle size) {
            this.pageSize = size;
        }

        public Rectangle getPageSize() {
            return this.pageSize;
        }

        public float getPageWidth() {
            return pageSize.getWidth();
        }

        public float getPageHeight() {
            return pageSize.getHeight();
        }

        public float getMarginLeft() {
            return marginLeft;
        }

        public void setMarginLeft(float marginLeft) {
            this.marginLeft = marginLeft;
        }

        public float getMarginTop() {
            return marginTop;
        }

        public void setMarginTop(float marginTop) {
            this.marginTop = marginTop;
        }

        public float getMarginRight() {
            return marginRight;
        }

        public void setMarginRight(float marginRight) {
            this.marginRight = marginRight;
        }

        public float getMarginBottom() {
            return marginBottom;
        }

        public void setMarginBottom(float marginBottom) {
            this.marginBottom = marginBottom;
        }

        public void setMargin(float margin) {
            this.marginLeft = margin;
            this.marginBottom = margin;
            this.marginRight = margin;
            this.marginTop = margin;
        }

        public void setMargin(float marginLeft, float marginTop, float marginRight, float marginBottom) {
            this.marginLeft = marginLeft;
            this.marginBottom = marginTop;
            this.marginRight = marginRight;
            this.marginTop = marginBottom;
        }

    }

    /**
     * 命名的流数据
     */
    public static class Flow {
        public static final String TITLE_FLOW = "title";
        public static final String SEPARATOR_FLOW = "line";
        public static final String QRCODE_FLOW = "qrcode";
        public static final String BARCODE_FLOW = "barcode";
        public static final String TABLE_FLOW = "table";
        public static final String LAYOUT_FLOW = "layout";
        public static final String CONTENT_FLOW = "content";
        public static final String LINEAR_FLOW = "linear";
        public static final String NEW_LINE = "newLine";
        public static final String NEW_PAGE = "newPage";
        public static final String IMAGE_FLOW = "image";
        public static final String RECTANGLE_FLOW = "rectangle";

        public Flow() {
        }

        /**
         * 用于分页、分割线的流定义
         **/
        public Flow(String name) {
            this.name = name;
        }

        public Flow(String name, FlowElement element) {
            this.name = name;
            this.element = element;
        }

        /**
         * 自定义 Pdf 流名称，用于指定什么参数集
         */
        private String name;

        /**
         * 参数集引用
         */
        private FlowElement element;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public FlowElement getElement() {
            return element;
        }

        public void setElement(FlowElement element) {
            this.element = element;
        }
    }

    public interface FlowElement {
        String ALIGN_LEFT = "ALIGN_LEFT";
        String ALIGN_CENTER = "ALIGN_CENTER";
        String ALIGN_RIGHT = "ALIGN_RIGHT";
        String ALIGN_JUSTIFIED = "ALIGN_JUSTIFIED";
        String ALIGN_TOP = "ALIGN_TOP";
        String ALIGN_MIDDLE = "ALIGN_MIDDLE";
        String ALIGN_BOTTOM = "ALIGN_BOTTOM";
        String ALIGN_BASELINE = "ALIGN_BASELINE";
        String ALIGN_JUSTIFIED_ALL = "ALIGN_JUSTIFIED_ALL";

        static int getAlignment(String align) {
            if (align == null || align.length() == 0) {
                return Element.ALIGN_CENTER;
            }
            if (ALIGN_LEFT.equals(align)) {
                return Element.ALIGN_LEFT;
            }
            if (ALIGN_CENTER.equals(align)) {
                return Element.ALIGN_CENTER;
            }
            if (ALIGN_RIGHT.equals(align)) {
                return Element.ALIGN_RIGHT;
            }
            if (ALIGN_JUSTIFIED.equals(align)) {
                return Element.ALIGN_JUSTIFIED;
            }
            if (ALIGN_TOP.equals(align)) {
                return Element.ALIGN_TOP;
            }
            if (ALIGN_MIDDLE.equals(align)) {
                return Element.ALIGN_MIDDLE;
            }
            if (ALIGN_BOTTOM.equals(align)) {
                return Element.ALIGN_BOTTOM;
            }
            if (ALIGN_BASELINE.equals(align)) {
                return Element.ALIGN_BASELINE;
            }
            if (ALIGN_JUSTIFIED_ALL.equals(align)) {
                return Element.ALIGN_JUSTIFIED_ALL;
            }

            return Element.ALIGN_UNDEFINED;
        }

        Flow flow();
    }

    /**
     * rectangle 矩形流实体定义
     */
    public static class RectangleFlowData implements FlowElement {

        private Float height;

        private Float width;

        private String color;

        public static RectangleFlowData build() {
            return new RectangleFlowData();
        }

        public RectangleFlowData setHeight(Float height) {
            this.height = height;
            return this;
        }

        public Float getHeight() {
            return this.height;
        }

        public RectangleFlowData setWidth(Float width) {
            this.width = width;
            return this;
        }

        public Float getWidth() {
            return this.width;
        }

        public RectangleFlowData setColor(String color) {
            this.color = color;
            return this;
        }

        public String getColor() {
            return this.color;
        }

        @Override
        public Flow flow() {
            return new Flow(Flow.RECTANGLE_FLOW, this);
        }
    }

    /**
     * 分隔线流实体定义
     */
    public static class SeparatorFlowData implements FlowElement {
        private String formatName;

        public String getFormatName() {
            return formatName;
        }

        public void setFormatName(String formatName) {
            this.formatName = formatName;
        }

        @Override
        public Flow flow() {
            return new Flow(Flow.SEPARATOR_FLOW);
        }
    }

    /**
     * 标题（文字）流实体定义，字体格式 及 对齐参数
     */
    public static class TitleFlowData implements FlowElement {
        private String content;
        private String formatName;
        private String alignment;

        public TitleFlowData() {
        }

        public TitleFlowData(String content, String formatName, String alignment) {
            this.content = content;
            this.formatName = formatName;
            this.alignment = alignment;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getFormatName() {
            return formatName;
        }

        public void setFormatName(String formatName) {
            this.formatName = formatName;
        }

        public String getAlignment() {
            return alignment;
        }

        public void setAlignment(String alignment) {
            this.alignment = alignment;
        }

        @Override
        public Flow flow() {
            return new Flow(Flow.TITLE_FLOW, this);
        }
    }


    /**
     * 二维码流实体定义（未实现）
     */
    public static class QRCodeFlowData implements FlowElement {
        private String code;
        private String formatName;

        public QRCodeFlowData() {
        }

        public QRCodeFlowData(String code, String formatName) {
            this.code = code;
            this.formatName = formatName;
        }

        public String getFormatName() {
            return formatName;
        }

        public void setFormatName(String formatName) {
            this.formatName = formatName;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        @Override
        public Flow flow() {
            return new Flow(Flow.QRCODE_FLOW, this);
        }
    }

    /**
     * 流实体定义
     */
    public static class ContentFlowData implements FlowElement {
        private Layout layout;
        private RowFormat format;
        private String[] title;
        private String[] data;
        private int verticalAlign;
        private int horizontalAlign;

        @Override
        public Flow flow() {
            return new Flow(Flow.CONTENT_FLOW, this);
        }

        public Layout getLayout() {
            return layout;
        }

        public void setLayout(Layout layout) {
            this.layout = layout;
        }

        public static ContentFlowData build() {
            return new ContentFlowData();
        }

        public ContentFlowData setLayout(float[] columnsWith) {
            layout = new Layout(columnsWith);
            return this;
        }

        public ContentFlowData rowFormat(String formatName, float height) {
            format = new RowFormat(formatName, height);
            return this;
        }

        public RowFormat getFormat() {
            return format;
        }

        public ContentFlowData setFormat(RowFormat format) {
            this.format = format;
            return this;
        }

        public String[] getTitle() {
            return title;
        }

        public ContentFlowData setTitle(String[] title) {
            this.title = title;
            return this;
        }

        public String[] getData() {
            return data;
        }

        public ContentFlowData setData(String[] data) {
            this.data = data;
            return this;
        }

        public int getVerticalAlign() {
            return verticalAlign;
        }

        public ContentFlowData setVerticalAlign(int verticalAlign) {
            this.verticalAlign = verticalAlign;
            return this;
        }

        public int getHorizontalAlign() {
            return horizontalAlign;
        }

        public ContentFlowData setHorizontalAlign(int horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            return this;
        }
    }

    /**
     * 表格流实体定义：包括，布局，格式以及表数据
     */
    public static class TableFlowData implements FlowElement {
        private Layout layout;
        private TableRowFormat format;
        private String header;
        private TableBorderFormat borderFormat;
        private String[] data;

        @Override
        public Flow flow() {
            return new Flow(Flow.TABLE_FLOW, this);
        }

        public static TableFlowData build() {
            return new TableFlowData();
        }

        public TableFlowData layout(float[] columnsWith) {
            layout = new Layout(columnsWith);
            return this;
        }

        public TableFlowData layout(Layout layout) {
            this.layout = layout;
            return this;
        }

        public TableFlowData borderFormat(Integer style, Integer width) {
            this.setBorderFormat(new TableBorderFormat(style, width));
            return this;
        }

        public TableFlowData data(String[] data) {
            this.data = data;
            return this;
        }

        public TableFlowData headerFormat(String formatName, float height) {
            return headerFormat(formatName, height, null);
        }

        public TableFlowData headerFormat(String formatName, float height, BaseColor color) {
            if (format == null) {
                format = new TableRowFormat();
            }
            format.setHeader(new RowFormat(formatName, height, color));
            return this;
        }

        public TableFlowData firstRowFormat(String formatName, float height) {
            return firstRowFormat(formatName, height, null);
        }

        public TableFlowData firstRowFormat(String formatName, float height, BaseColor color) {
            if (format == null) {
                format = new TableRowFormat();
            }
            format.setFirstRowFormat(new RowFormat(formatName, height, color));
            return this;
        }

        public TableFlowData rowFormat(String formatName, float height) {
            return rowFormat(formatName, height, null);
        }

        public TableFlowData rowFormat(String formatName, float height, BaseColor color) {
            if (format == null) {
                format = new TableRowFormat();
            }
            format.setRowFormat(new RowFormat(formatName, height, color));
            return this;
        }


        public TableBorderFormat getBorderFormat() {
            return borderFormat;
        }

        public void setBorderFormat(TableBorderFormat borderFormat) {
            this.borderFormat = borderFormat;
        }

        public Layout getLayout() {
            return layout;
        }

        public void setLayout(Layout layout) {
            this.layout = layout;
        }

        public TableRowFormat getFormat() {
            return format;
        }

        public void setFormat(TableRowFormat format) {
            this.format = format;
        }

        public String[] getData() {
            return data;
        }

        public TableFlowData setData(String[] data) {
            this.data = data;
            return this;
        }

        public String getHeader() {
            return header;
        }

        public TableFlowData setHeader(String header) {
            this.header = header;
            return this;
        }

        public static class TableBorderFormat {
            private Integer style;
            private Integer width;

            public TableBorderFormat() {
            }

            public TableBorderFormat(Integer style, Integer width) {
                this.style = style;
                this.width = width;
            }

            public Integer getStyle() {
                return style;
            }

            public void setStyle(Integer style) {
                this.style = style;
            }

            public Integer getWidth() {
                return width;
            }

            public void setWidth(Integer width) {
                this.width = width;
            }
        }

        /**
         * 表格格式, 包括(表头格式，第一行格式，其他行格式)三部分格式
         * 第一行 格式包括： 行高 + 字体
         */
        public static class TableRowFormat {
            private RowFormat header;
            private RowFormat firstRowFormat;
            private RowFormat rowFormat;

            public RowFormat getHeader() {
                return header;
            }

            public void setHeader(RowFormat header) {
                this.header = header;
            }

            public RowFormat getFirstRowFormat() {
                return firstRowFormat;
            }

            public void setFirstRowFormat(RowFormat firstRowFormat) {
                this.firstRowFormat = firstRowFormat;
            }

            public RowFormat getRowFormat() {
                return rowFormat;
            }

            public void setRowFormat(RowFormat rowFormat) {
                this.rowFormat = rowFormat;
            }
        }
    }


    /**
     * 流布局，至上而下(定义列数为1时)
     * 横向布局 使布局横向分配
     * <p>
     * 支持嵌套一层列数不为1 的流式布局
     */
    public static class LinearFlowData implements FlowElement {
        private List<Flow> elements = new ArrayList<>();
        private Layout layout;

        public LinearFlowData() {
        }

        public LinearFlowData(float[] columnsWith) {
            layout = new Layout();
            layout.setColumnWidths(columnsWith);
        }

        @Override
        public Flow flow() {
            return new Flow(Flow.LINEAR_FLOW, this);
        }

        public static LinearFlowData build() {
            return new LinearFlowData();
        }

        public LinearFlowData add(Flow element) {
            elements.add(element);
            return this;
        }

        public List<Flow> getElements() {
            return elements;
        }

        public void setElements(List<Flow> elements) {
            this.elements = elements;
        }

        public Layout getLayout() {
            return layout;
        }

        public LinearFlowData setLayout(float[] columnsWiths) {
            this.layout = new Layout(columnsWiths);
            return this;
        }

        public void setLayout(Layout layout) {
            this.layout = layout;
        }
    }

    /**
     * 图片
     **/
    public static class ImageFlowData implements FlowElement {
        private byte[] data;
        private String url;
        private float width;
        private float height;

        public ImageFlowData(byte[] data) {
            this.data = data;
        }

        public ImageFlowData(byte[] data, float width, float height) {
            this.data = data;
            this.width = width;
            this.height = height;
        }

        public ImageFlowData(String url) {
            this.url = url;
        }

        public ImageFlowData(String url, float width, float height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }

        @Override
        public Flow flow() {
            return new Flow(Flow.IMAGE_FLOW, this);
        }

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

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }
    }

    /**
     * 布局,可台定义列数（宽度平分），也可以定义不同的列宽
     */
    public static class Layout {
        private int numColumns;
        private float[] columnWidths;

        @Deprecated
        public int getNumColumns() {
            return numColumns;
        }

        @Deprecated
        public void setNumColumns(int numColumns) {
            this.numColumns = numColumns;
        }

        public Layout() {
        }

        public Layout(float[] columnWidths) {
            this.columnWidths = columnWidths;
        }

        public float[] getColumnWidths() {
            return columnWidths;
        }

        public void setColumnWidths(float[] columnWidths) {
            this.columnWidths = columnWidths;
        }
    }

    /**
     * Table Row format
     * 包括行高，Definitions中定义的格式名称
     */
    public static class RowFormat {
        private float height;
        private String formatName;
        private BaseColor color;

        public RowFormat() {
        }

        public RowFormat(String formatName, float height) {
            this.height = height;
            this.formatName = formatName;
        }

        public RowFormat(String formatName, float height, BaseColor color) {
            this.height = height;
            this.formatName = formatName;
            this.color = color;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public String getFormatName() {
            return formatName;
        }

        public void setFormatName(String formatName) {
            this.formatName = formatName;
        }

        public BaseColor getColor() {
            return color;
        }

        public void setColor(BaseColor color) {
            this.color = color;
        }
    }


    /**
     * 预定义格式，如字体，边界
     * 通过名称，获取具体格式参数
     */
    public static class Definitions {

        public Definitions() {
        }

        public Definitions(Map<String, FontDefinition> fonts, Map<String, BorderDefinition> borders) {
            this.fonts = fonts;
            this.borders = borders;
        }

        /**
         * 字体定义
         */
        private Map<String, FontDefinition> fonts;

        private Map<String, BorderDefinition> borders;

        public Map<String, FontDefinition> getFonts() {
            return fonts;
        }

        public void setFonts(Map<String, FontDefinition> fonts) {
            this.fonts = fonts;
        }

        public Map<String, BorderDefinition> getBorders() {
            return borders;
        }

        public void setBorders(Map<String, BorderDefinition> borders) {
            this.borders = borders;
        }
    }
}
