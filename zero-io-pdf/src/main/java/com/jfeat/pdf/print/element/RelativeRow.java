package com.jfeat.pdf.print.element;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.jfeat.pdf.print.base.ListRow;
import com.jfeat.pdf.print.base.RelativeListRow;

/**
 * Created by vincent on 2018/3/16.
 * 标准列表布局，标题，副标题，图标
 */
public class RelativeRow extends RelativeListRow implements ListRow {

    public static String ID = "RelativeRow";

    @Override
    public String rowId() {
        return ID;
    }

    protected Image icon;
    protected Image next;     // next image
    protected Phrase title;
    protected Phrase subtitle;
    protected Phrase hint;
    protected Phrase value;   // right content

    protected float titleSpacing = 0f;
    protected float titleIndent = 0f;
    protected int titleAlignment = Element.ALIGN_BASELINE;
    // for vertical center, titleAlignment = Element.ALIGN_LEFT

    public RelativeRow(Rectangle position) {
        super(position);
    }

    public RelativeRow() {
        super(new Rectangle(0, 0, 0, 0));
    }


    public RelativeRow title(String title, String subtitle, String hint,
                             Font font, Font subFont, Font hintFont) {
        this.title = new Phrase(title, font);
        if (subtitle != null) {
            this.subtitle = new Phrase(subtitle, subFont);
        }
        if (hint != null) {
            this.hint = new Phrase(hint, hintFont);
        }

        return this;
    }

    public RelativeRow value(String value, Font font) {
        this.value = new Phrase(value, font);
        ;
        return this;
    }

    /**
     * override by child to change iconAlignment
     *
     * @return
     */
    protected int iconAlignment() {
        return Element.ALIGN_LEFT;
    }

    public RelativeRow icon(Image icon) {
        this.icon = icon;
        this.icon.scaleAbsolute(-1, -1);
        this.icon.setAlignment(iconAlignment());
        return this;
    }

    public RelativeRow icon(Image icon, float width, float height) {
        this.icon = icon;
        this.icon.scaleAbsolute(width, height);
        this.icon.setAlignment(iconAlignment());
        return this;
    }

    public RelativeRow next(Image next) {
        this.next = next;
        this.next.scaleAbsolute(-1, -1);
        this.next.setAlignment(Element.ALIGN_RIGHT);
        return this;
    }

    public RelativeRow next(Image next, float width, float height) {
        this.next = next;
        this.next.scaleAbsolute(width, height);
        this.next.setAlignment(Element.ALIGN_RIGHT);
        return this;
    }

    @Override
    public void draw(PdfContentByte canvas, Rectangle position) {
        PdfContentByte[] canvases = new PdfContentByte[]{canvas, canvas, canvas, canvas};

        /// draw background
        canvas.saveState();
        canvas.rectangle(this);
        canvas.restoreState();

        drawCell(canvases, position);
    }

    @Override
    public void drawCell(PdfContentByte[] canvases, Rectangle position) {
        PdfContentByte bgcanvas = canvases[PdfPTable.BACKGROUNDCANVAS];

        // draw cell stroke
        bgcanvas.rectangle(position);

        // draw images
        bgcanvas.saveState();

        /// draw images
        drawIcon(bgcanvas, position, icon);
        drawIcon(bgcanvas, position, next);

        bgcanvas.restoreState();


        /**
         * draw text
         */

        PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
        canvas.saveState();

        // draw lines
        {
            Rectangle linesPosition = new Rectangle(position);

            /// fix position
            if (icon != null) {
                linesPosition.setLeft(position.getLeft() + icon.getScaledWidth());
            }
            if (next != null) {
                linesPosition.setRight(position.getRight() - next.getScaledWidth());
            }

            if (title != null && subtitle != null && hint != null) {
                drawLines(canvas, linesPosition, new Phrase[]{title, subtitle, hint}, titleAlignment, titleIndent, titleSpacing);
            } else if (title != null && subtitle != null) {
                drawLines(canvas, linesPosition, new Phrase[]{title, subtitle}, titleAlignment, titleIndent, titleSpacing);
            } else if (title != null) {
                drawLines(canvas, linesPosition, new Phrase[]{title}, titleAlignment, 0, 0);
            }
        }

        /// draw value text only if no next image
        if (next == null) {
            drawText(canvas, position, value, Element.ALIGN_RIGHT);
        }

        canvas.restoreState();
    }

    public Image getIcon() {
        return this.icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public Image getNext() {
        return this.next;
    }

    public void setNext(Image next) {
        this.next = icon;
    }

    public Phrase getHint() {
        return hint;
    }

    public void setHint(Phrase hint) {
        this.hint = hint;
    }

    public float getTitleSpacing() {
        return this.titleSpacing;
    }

    public void setTitleSpacing(float spacing) {
        this.titleSpacing = spacing;
    }

    public float getTitleIndent() {
        return this.titleIndent;
    }

    public void setTitleIndent(float indent) {
        this.titleIndent = indent;
    }

    public int getTitleAlignment() {
        return titleAlignment;
    }

    public void setTitleAlignment(int titleAlignment) {
        this.titleAlignment = titleAlignment;
    }
}
