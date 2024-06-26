package com.jfeat.pdf.print.element;

import cn.hutool.core.util.StrUtil;
import com.itextpdf.awt.AsianFontMapper;
import com.itextpdf.awt.FontMapper;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.jfeat.pdf.print.base.ListRow;
import com.jfeat.pdf.print.base.PaddingListRow;
import com.jfeat.pdf.print.base.TableCellElement;
import com.jfeat.pdf.print.util.PdfFontMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vincent on 2018/3/16.
 * 文本框: 纯色框内居中显示字符
 */
public class TextBox extends TableCellElement implements ListRow {
    public static String ID = "TextBox";

    @Override
    public String rowId() {
        return ID;
    }

    protected String content;
    protected Font font;

    protected float spacing = 0f;
    protected float indent = 0f;
    protected int alignment = Element.ALIGN_CENTER;
    protected int verticalAlignment = Element.ALIGN_MIDDLE;


    public Font getFont() {
        return font;
    }

    public void setFont(Font textFont) {
        this.font = textFont;
    }

    public String getContent() {
        return content;
    }

    /**
     * 左右间距
     */
    public TextBox() {
        super(0, 0, 0, 0);
    }

    public TextBox(int horizontalAlignment) {
        super(0, 0, 0, 0);
        this.alignment = horizontalAlignment;
    }

    public TextBox(int horizontalAlignment, int verticalAlignment) {
        super(0, 0, 0, 0);
        this.alignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
    }

    public TextBox(String content, Font font) {
        super(0, 0, 0, 0);
        this.content = content;
        this.font = font;
    }

    public TextBox(String content, Font font, int horizontalAlignment, int verticalAlignment) {
        super(0, 0, 0, 0);
        this.content = content;
        this.font = font;
        this.alignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
    }

    public TextBox(Rectangle position, String content, Font font) {
        super(position);
        this.content = content;
        this.font = font;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setContent(String content, Font font) {
        this.content = content;
        this.font = font;
    }

    public void drawCell(PdfContentByte[] canvases, Rectangle position) {
        if (content == null || content.length() == 0) {
            return;
        }

        super.setLeft(position.getLeft());
        super.setTop(position.getTop());
        super.setRight(position.getRight());
        super.setBottom(position.getBottom());

        // draw background
        PdfContentByte bgcanvas = canvases[PdfPTable.BACKGROUNDCANVAS];
        bgcanvas.saveState();
        Rectangle solid = new Rectangle(this);
        float borderWidth = solid.getBorderWidth();
        if (borderWidth > 0) {
            solid.setLeft(solid.getLeft() + borderWidth);
            solid.setRight(solid.getRight() - borderWidth);
            solid.setTop(solid.getTop() - borderWidth);
            solid.setBottom(solid.getBottom() + borderWidth);
        }
        bgcanvas.rectangle(solid);
        bgcanvas.restoreState();


        /// draw text
        PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];

        // draw text via PdfGraphics2D
        int stringHeight = 0;
        int stringWidth = 0;
        PdfFontMetrics metrics = new PdfFontMetrics(canvas, font);
        {
            //stringWidth = metrics.getStringWidth(content);
            stringHeight = metrics.getStringHeight();
            stringWidth = metrics.getStringWidth(content);

            //java.awt.Font font = new java.awt.Font(textFont.getFamilyname(), textFont.getStyle(), (int) textFont.getSize());
            //PdfGraphics2D graphics2D = createGraphics2D(canvas);
            //graphics2D.setFont(font);
            //FontMetrics fm = graphics2D.getFontMetrics(font);
            //stringWidth = fm.stringWidth(content);
            //stringHeight = fm.getDescent()-fm.getAscent();

            ////float cx = (position.getLeft()+position.getRight())*0.5f;
            ////float cy = (position.getTop()+position.getBottom())*0.5f;
            ////graphics2D.drawString(content, cx, cy);
        }

        ///draw text
        canvas.saveState();

        float tx;
        switch (alignment) {
            case Element.ALIGN_LEFT:
                tx = getLeft();
                break;
            case Element.ALIGN_RIGHT:
                tx = getRight();
                break;
            case Element.ALIGN_CENTER:
            default: // else ALIGN_CENTER
                tx = (getLeft() + getRight()) * 0.5f;
                break;
        }

        tx += (paddingLeft + paddingRight);

        // int contentLen = (int)(stringWidth / getWidth()) + (stringWidth%getWidth()>0?1:0);
        final int stringYOffset = 4;

        // get text array
        List<String> lines = alignUpPosition(metrics, content, this, paddingLeft, paddingRight);

        int contentLen = lines.size();
        //String[] lines = new String[] {content.substring(0,5), content.substring(6,content.length())};


        float boxHeight = getHeight();
        /*int textTotalHeight = 0, limitContentLen = 0, k;
        for (int i = 1; i <= contentLen; i++) {
            if ((k = (stringHeight * i + stringYOffset * (i - 1))) > boxHeight) {
                break;
            }
            limitContentLen++;
            textTotalHeight = k;
        }*/


        /// logger.info("textTotalHeight {}", textTotalHeight);
        /// logger.info("limitContentLen : {}", limitContentLen);
        int textTotalHeight = stringHeight * contentLen + stringYOffset * (contentLen - 1);
        float offset = (boxHeight - textTotalHeight) * 0.5f;

        logger.info("boxHeight {}", boxHeight);
        logger.info("stringHeight {}", stringHeight);
        logger.info("textTotalHeight {}", textTotalHeight);
        logger.info("offset {}", offset);

        if (verticalAlignment == Element.ALIGN_TOP) {
            offset = 0;
        }
        for (int i = 0; i < contentLen; i++) {
            String text = lines.get(i);
            float ty = getTop() - offset - stringHeight * 0.5f - (stringHeight * i + stringYOffset * i);
            ty -= stringHeight * 0.5f;  // offset string vertical center

            //float ty_old = (getTop() + getBottom()) * 0.5f - stringHeight * 0.5f;

            ColumnText.showTextAligned(canvas, alignment, new Phrase(text, font), tx, ty, 0);
        }

        canvas.restoreState();
    }

    protected final static Logger logger = LoggerFactory.getLogger(TextBox.class);

    /**
     * 单行在限定框内转换为多行
     *
     * @param content
     * @param position 矩形位置
     * @return
     */
    public static List<String> alignUpPosition(PdfFontMetrics metrics, String content, Rectangle position, float paddingLeft, float paddingRight) {

        float totalWidth = position.getWidth() - paddingLeft + paddingRight;
        float totalHeight = position.getHeight();
        logger.info("totalWidth : {}, totalHeight : {}", totalWidth, totalHeight);
        // 垂直间距
        int verticalOffset = 4;
        // 字高
        int stringHeight = metrics.getStringHeight();
        // 根据高度计算最大行数
        int maxLine = (int) Math.floor
                ((totalHeight + verticalOffset) / (stringHeight + verticalOffset));

        List<String> lines = new ArrayList<>();
        int len = content.length();

        // 对文本内容进行分行
        int i = 0, j = 0, currWidth = 0;
        while (i < len) {
            int charWidth = metrics.getFontStringWidth(String.valueOf(content.charAt(i)));
            if (charWidth > totalWidth) {
                throw new RuntimeException("字符宽度不能大于限定框宽度");
            }
            currWidth += charWidth;
            /// logger.info("curWidth total --> {}, {}", currWidth, totalWidth);
            logger.info("charWidth : {}", charWidth);
            if (currWidth > totalWidth) {
                lines.add(content.substring(j, i));
                j = i;
                currWidth = 0;
                continue;
            }
            i++;
        }
        // 最后一行
        lines.add(content.substring(j));

        // 超出限定框高度限制
        if (!lines.isEmpty() && lines.size() > maxLine) {
            logger.debug("超出限定框高度限制");
            // 删除超出高度的行
            lines = lines.subList(0, maxLine);
            // 被省略号替换的字符长度
            int replaceLen = 3;
            String last = null;
            // 在最后一行加上省略号
            if (!lines.isEmpty() && (last = lines.remove(maxLine - 1)).length() > replaceLen) {
                last = StrUtil.subPre(last, last.length() - replaceLen) + "...";
                lines.add(last);
            }
            logger.info("last line : {}", last);
        }

        logger.info("lines size: {}", lines.size());
        logger.info("lines array : {}", lines);
        logger.info("metrics.getStringWidth(content) : {}", metrics.getFontStringWidth(content));
        logger.info("totalWidth: {}", totalWidth);

        return lines;
    }


    /**
     * implement in FontSizeMetrics
     *
     * @param canvas
     * @return
     */
    @Deprecated
    private PdfGraphics2D createGraphics2D(PdfContentByte canvas) {

        Document document = canvas.getPdfDocument();
        Rectangle pageSize = document.getPageSize(); //获取pdf文档的大小

        //添加关于中文的支持
        FontMapper fm = new AsianFontMapper(AsianFontMapper.ChineseSimplifiedFont, AsianFontMapper.ChineseSimplifiedEncoding_H);
        PdfGraphics2D graphics2D = new PdfGraphics2D(canvas, pageSize.getWidth(), pageSize.getHeight(), fm);

        return graphics2D;
    }

    public float getSpacing() {
        return spacing;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
    }

    public float getIndent() {
        return indent;
    }

    public void setIndent(float indent) {
        this.indent = indent;
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(int alignment) {
        this.verticalAlignment = alignment;
    }
}
