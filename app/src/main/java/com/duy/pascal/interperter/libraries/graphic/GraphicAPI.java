/*
 *  Copyright 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.pascal.interperter.libraries.graphic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.duy.pascal.ui.utils.DLog;
import com.duy.pascal.ui.runnable.IConsoleHandler;
import com.duy.pascal.ui.view.console.ConsoleCursor;
import com.duy.pascal.ui.view.console.ConsoleView;
import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContextMixin;
import com.duy.pascal.interperter.ast.runtime.references.PascalReference;
import com.duy.pascal.interperter.ast.runtime.value.RecordValue;
import com.duy.pascal.interperter.ast.variablecontext.ContainsVariables;
import com.duy.pascal.interperter.libraries.PascalLibrary;
import com.duy.pascal.interperter.libraries.annotations.PascalMethod;
import com.duy.pascal.interperter.libraries.crt.ColorUtils;
import com.duy.pascal.interperter.libraries.crt.CrtLib;
import com.duy.pascal.interperter.libraries.graphic.model.ArcEllipseObject;
import com.duy.pascal.interperter.libraries.graphic.model.ArcObject;
import com.duy.pascal.interperter.libraries.graphic.model.Bar3DObject;
import com.duy.pascal.interperter.libraries.graphic.model.BarObject;
import com.duy.pascal.interperter.libraries.graphic.model.CircleObject;
import com.duy.pascal.interperter.libraries.graphic.model.EllipseObject;
import com.duy.pascal.interperter.libraries.graphic.model.FillEllipseObject;
import com.duy.pascal.interperter.libraries.graphic.model.LineObject;
import com.duy.pascal.interperter.libraries.graphic.model.PieSliceObject;
import com.duy.pascal.interperter.libraries.graphic.model.PixelObject;
import com.duy.pascal.interperter.libraries.graphic.model.PolygonObject;
import com.duy.pascal.interperter.libraries.graphic.model.RectangleObject;
import com.duy.pascal.interperter.libraries.graphic.model.SectorObject;
import com.duy.pascal.interperter.libraries.graphic.model.TextGraphObject;
import com.duy.pascal.interperter.libraries.graphic.paint.TextPaint;
import com.duy.pascal.interperter.libraries.graphic.style.FillType;
import com.duy.pascal.interperter.libraries.graphic.style.LineStyle;
import com.duy.pascal.interperter.libraries.graphic.style.LineWidth;
import com.duy.pascal.interperter.libraries.graphic.style.TextDirection;
import com.duy.pascal.interperter.libraries.graphic.style.TextFont;
import com.duy.pascal.interperter.libraries.graphic.style.TextJustify;
import com.duy.pascal.interperter.declaration.Name;
import com.duy.pascal.interperter.declaration.lang.types.BasicType;
import com.duy.pascal.interperter.declaration.lang.types.RecordType;
import com.duy.pascal.interperter.declaration.lang.value.ConstantDefinition;
import com.duy.pascal.interperter.declaration.lang.value.VariableDeclaration;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;
import com.duy.pascal.interperter.imageprocessing.FloodFill;
import com.duy.pascal.interperter.linenumber.LineNumber;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Duy on 01-Mar-17.
 */
@SuppressWarnings("unused")
public class GraphicAPI extends PascalLibrary {
    public static final String NAME = "graph";
    private static final String TAG = "GraphLib";
    @NonNull
    private IConsoleHandler handler;
    private GraphScreen mGraphScreen;

    public GraphicAPI() {

    }

    public GraphicAPI(@NonNull IConsoleHandler handler) {
        this.handler = handler;
        if (handler.getConsoleView() != null) {
            this.mGraphScreen = handler.getConsoleView().getGraphScreen();
        }
    }

    @Override
    @PascalMethod(description = "stop")

    public void onFinalize() {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void declareConstants(ExpressionContextMixin context) {
        Map<Name, ConstantDefinition> constants = context.getConstants();
        ConstantDefinition constant;
        constant = new ConstantDefinition("grok".toLowerCase(), 1, new LineNumber(-1, "grok = 1;".toLowerCase()));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("NormWidth".toLowerCase(), LineWidth.NormWidth,
                new LineNumber(-1, "const NormWidth = 1;".toLowerCase()));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("ThickWidth".toLowerCase(), LineWidth.ThickWidth,
                new LineNumber(-1, ""));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("SolidLn".toLowerCase(), LineStyle.SolidLn,
                new LineNumber(-1, "const SolidLn = 0;".toLowerCase()));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("DottedLn".toLowerCase(), LineStyle.DottedLn,
                new LineNumber(-1, "const DottedLn = 1;".toLowerCase()));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("CenterLn".toLowerCase(), LineStyle.CenterLn,
                new LineNumber(-1, "const CenterLn = 2;".toLowerCase()));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("DashedLn".toLowerCase(), LineStyle.DashedLn,
                new LineNumber(-1, "const DashedLn = 3;".toLowerCase()));
        constants.put(constant.getName(), constant);

        //Font number: Normal font
        constant = new ConstantDefinition("DefaultFont".toLowerCase(), TextFont.TriplexFont,
                new LineNumber(-1, "const DefaultFont = 0;".toLowerCase()));
        constants.put(constant.getName(), constant);

        //        Font number: Triplex font
        constant = new ConstantDefinition("TriplexFont".toLowerCase(), TextFont.TriplexFont,
                new LineNumber(-1, "const TriplexFont = 1;".toLowerCase()));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("SmallFont".toLowerCase(), TextFont.SmallFont,
                new LineNumber(-1, "const SmallFont = 2;".toLowerCase()));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("SansSerifFont".toLowerCase(), TextFont.SansSerifFont,
                new LineNumber(-1, "const SansSerifFont = 3;".toLowerCase()));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("GothicFont".toLowerCase(), TextFont.GothicFont,
                new LineNumber(-1, "const GothicFont = 4;".toLowerCase()));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("ScriptFont".toLowerCase(), TextFont.ScriptFont,
                new LineNumber(-1, "const ScriptFont = 5;".toLowerCase()));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("SimpleFont".toLowerCase(), TextFont.SimpleFont,
                new LineNumber(-1, "const SimpleFont = 6;".toLowerCase()));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("TSCRFont".toLowerCase(), TextFont.TSCRFont,
                new LineNumber(-1, "const TSCRFont = 7;".toLowerCase()));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("LCOMFont".toLowerCase(), TextFont.LCOMFont,
                new LineNumber(-1, "const LCOMFont = 8;".toLowerCase()));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("EuroFont".toLowerCase(), TextFont.EuroFont,
                new LineNumber(-1, "const EuroFont  = 9;".toLowerCase()));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("BoldFont".toLowerCase(), TextFont.BoldFont,
                new LineNumber(-1, "const EuroFont  = 10;".toLowerCase()));
        constants.put(constant.getName(), constant);

        //text direction
        constant = new ConstantDefinition("HorizDir".toLowerCase(), TextDirection.HORIZONTAL_DIR,
                new LineNumber(-1, "const HorizDir = 0;".toLowerCase()));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("VertDir".toLowerCase(), TextDirection.VERTICAL_DIR,
                new LineNumber(-1, "const VertDir   = 1;".toLowerCase()));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("HorizDir".toLowerCase(), TextDirection.HORIZONTAL_DIR,
                new LineNumber(-1, "const HorizDir = 0;".toLowerCase()));
        constants.put(constant.getName(), constant);

        ///////////////////////////
        constant = new ConstantDefinition("LeftText".toLowerCase(), TextJustify.HORIZONTAL_STYLE.LeftText,
                new LineNumber(-1, "const LeftText   = 0;".toLowerCase()));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("CenterText".toLowerCase(), TextJustify.HORIZONTAL_STYLE.CenterText,
                new LineNumber(-1, "const CenterText   = 1;".toLowerCase()));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("RightText".toLowerCase(), TextJustify.HORIZONTAL_STYLE.RightText,
                new LineNumber(-1, "const RightText   = 2;".toLowerCase()));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("BottomText".toLowerCase(), TextJustify.VERTICAL_STYLE.BottomText,
                new LineNumber(-1, "const BottomText   = 0;".toLowerCase()));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("TopText".toLowerCase(), TextJustify.VERTICAL_STYLE.TopText,
                new LineNumber(-1, "const TopText   = 2;".toLowerCase()));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("topoff".toLowerCase(), false,
                new LineNumber(-1, "const TopText   = 2;".toLowerCase()));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("topon".toLowerCase(), true,
                new LineNumber(-1, "const TopText   = 2;".toLowerCase()));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("EmptyFill".toLowerCase(), FillType.EmptyFill,
                new LineNumber(-1, "grok = 1;".toLowerCase()));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("SolidFill".toLowerCase(), FillType.SolidFill,
                new LineNumber(-1, "const NormWidth = 1;".toLowerCase()));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("LineFill".toLowerCase(), FillType.LineFill,
                new LineNumber(-1, ""));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("ltSlashFill".toLowerCase(), FillType.ltSlashFill,
                new LineNumber(-1, ""));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("SlashFill".toLowerCase(), FillType.SlashFill,
                new LineNumber(-1, ""));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("BkSlashFill".toLowerCase(), FillType.BkSlashFill,
                new LineNumber(-1, ""));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("LtBkSlashFill".toLowerCase(), FillType.LtBkSlashFill,
                new LineNumber(-1, ""));
        constants.put(constant.getName(), constant);
        constant = new ConstantDefinition("HatchFill".toLowerCase(), FillType.HatchFill,
                new LineNumber(-1, ""));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("XHatchFill".toLowerCase(), FillType.XHatchFill,
                new LineNumber(-1, ""));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("InterLeaveFill".toLowerCase(), FillType.InterLeaveFill,
                new LineNumber(-1, ""));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("WideDotFill".toLowerCase(), FillType.WideDotFill,
                new LineNumber(-1, ""));
        constants.put(constant.getName(), constant);

        constant = new ConstantDefinition("CloseDotFill".toLowerCase(), FillType.CloseDotFill,
                new LineNumber(-1, ""));
        constants.put(constant.getName(), constant);

        new CrtLib(handler).declareConstants(context);

        constant = new ConstantDefinition("NormalPut".toLowerCase(), 0, new LineNumber(-1, ""));
        constants.put(constant.getName(), constant);
    }

    @Override
    public void declareTypes(ExpressionContextMixin context) {
        RecordType arcType = new RecordType();
        ArrayList<VariableDeclaration> vars = new ArrayList<>();
        vars.add(new VariableDeclaration("x", BasicType.Integer));
        vars.add(new VariableDeclaration("y", BasicType.Integer));
        vars.add(new VariableDeclaration("xstart", BasicType.Integer));
        vars.add(new VariableDeclaration("ystart", BasicType.Integer));
        vars.add(new VariableDeclaration("xend", BasicType.Integer));
        vars.add(new VariableDeclaration("yend", BasicType.Integer));
        arcType.setVariableDeclarations(vars);
        context.declareTypedef("ArcCoordsType", arcType);

        RecordType point = new RecordType();
        vars = new ArrayList<>();
        vars.add(new VariableDeclaration("x", BasicType.Integer));
        vars.add(new VariableDeclaration("y", BasicType.Integer));
        point.setVariableDeclarations(vars);
        context.declareTypedef("pointtype", point);

    }

    @Override
    public void declareVariables(ExpressionContextMixin context) {

    }

    @Override
    public void declareFunctions(ExpressionContextMixin context) {

    }

    /**
     * ArcObject draws part of a circle with center at (X,Y),
     * radius radius, starting from angle start, stopping at angle stop. T
     * hese angles are measured counterclockwise.
     */

    @PascalMethod(description = "graph library")
    public void arc(int x, int y, int stAngle, int endAngle, int radius) {
        if (handler.getConsoleView() != null) {

            handler.getConsoleView().getGraphScreen()
                    .addGraphObject(new ArcObject(x, y, stAngle, endAngle, radius));
        }
    }

    /**
     * Draws a rectangle with corners at (X1,Y1) and (X2,Y2) and fills
     * it with the current color and fill-style.
     */

    @PascalMethod(description = "graph library")
    public void bar(int x1, int y1, int x2, int y2) {
        if (handler.getConsoleView() != null) {
            handler.getConsoleView().getGraphScreen()
                    .addGraphObject(new BarObject(x1, y1, x2, y2));
        }
    }


    @PascalMethod(description = "Return maximal X coordinate")
    public int getMaxX() {
        return handler.getConsoleView().getWidth();
    }


    @PascalMethod(description = "Return maximal Y coordinate")
    public int getMaxY() {
        return handler.getConsoleView().getHeight();
    }


    @PascalMethod(description = "Initialize graphical system")
    public void initGraph(int driver, int mode, StringBuilder pathToDriver) {
        if (handler.getConsoleView() != null) {

            handler.getConsoleView().setGraphicMode(true);
        }
    }


    /**
     * Draw a rectangle on the screen
     */

    @PascalMethod(description = "Draw a rectangle on the screen")
    public void rectangle(int x1, int y1, int x2, int y2) {
        if (handler.getConsoleView() != null)
            mGraphScreen.addGraphObject(new RectangleObject(x1, y1, x2, y2));
    }


    @PascalMethod(description = "graph library")
    public void line(int x1, int y1, int x2, int y2) {
        if (handler.getConsoleView() != null)
            mGraphScreen.addGraphObject(new LineObject(x1, y1, x2, y2));
    }

    /**
     * GetY returns the Y-coordinate of the current position of the graphical cursor
     */

    @PascalMethod(description = "graph library")
    public int getY() {
        if (handler.getConsoleView() != null) {
            return handler.getConsoleView().getYCursorPixel();
        } else {
            return 0;
        }
    }

    /**
     * GetY returns the Y-coordinate of the current position of the graphical cursor
     */

    @PascalMethod(description = "graph library")
    public int getX() {
        if (handler.getConsoleView() != null) {
            return handler.getConsoleView().getXCursorPixel();
        } else {
            return 0;
        }
    }

    /**
     * @return color in pixel x, y of screen
     */

    @PascalMethod(description = "graph library")
    public int getPixel(int x, int y) {
        if (handler.getConsoleView() == null) return -1;

        GraphScreen graphScreen = handler.getConsoleView().getGraphScreen();
        int colorPixel = graphScreen.getColorPixel(x, y);
        return ColorUtils.androidColorToPascalColor(colorPixel);
    }

    /**
     * Closes the graphical system, and restores the screen modus which was active before
     * the graphical modus was activated.
     */

    @PascalMethod(description = "graph library")
    public void closeGraph() {
        ConsoleView consoleView = handler.getConsoleView();
        if (consoleView != null) {
            consoleView.closeGraphic();
        }
    }

    /**
     * GetColor returns the current drawing color (the palette entry).
     */

    @PascalMethod(description = "graph library")
    public int getColor() {
        if (handler.getConsoleView() != null)
            return handler.getConsoleView().getForegroundGraphColor();
        else
            return 0; //black
    }

    /**
     * Set foreground drawing color
     */
    @PascalMethod(description = "Set foreground drawing color")
    public void setColor(int index) {
        mGraphScreen.setPaintColor(ColorUtils.pascalColorToAndroidColor(index));
    }

    @PascalMethod(description = "Clears the graphical screen (with the current background color), and sets the cursor at (0,0).")
    public void clearDevice() {
        handler.getConsoleView().clearGraphic();
    }

    @PascalMethod(description = "moves the cursor to the point (X,Y).")
    public void moveTo(int x, int y) {
        handler.getConsoleView().setCursorGraphPosition(x, y);
    }

    /**
     * DetectGraph checks the hardware in the PC and determines the driver and screen-modus
     * to be used. These are returned in Driver and Modus, and can be fed to InitGraph. See
     * the InitGraph for a list of drivers and module.
     */

    @PascalMethod(description = "graph library")
    public void detectGraph(PascalReference<Integer> driver, PascalReference<Integer> mode) {
        driver.set(0);
        mode.set(0);
    }

    /**
     * MoveRel moves the cursor to the point (DX,DY), relative to the current cursor position
     */

    @PascalMethod(description = "graph library")
    public void moveRel(int dx, int dy) {
        ConsoleCursor point = handler.getConsoleView().getCursorGraphic();
        handler.getConsoleView().setCursorGraphPosition(point.x + dx, point.y + dy);
    }


    @PascalMethod(description = "graph library")
    public int detect() {
        return 1;
    }


    @PascalMethod(description = "graph library")
    public Integer graphResult() {
        return 1;
    }


    @PascalMethod(description = "graph library")
    public void circle(int x, int y, int r) {
        if (handler.getConsoleView() != null)
            mGraphScreen.addGraphObject(new CircleObject(x, y, r));
    }

    /**
     * LineTo draws a line starting from the current cursor position to the point(DX,DY),
     * relative to the current position, in the current line style and color. The Current
     * position is set to the end of the line.
     */

    @PascalMethod(description = "graph library")
    public void lineTo(int x, int y) {
        if (handler.getConsoleView() != null) {

            ConsoleCursor point = handler.getConsoleView().getCursorGraphic();
            mGraphScreen.addGraphObject(new LineObject(point.x, point.y, x, y));
            handler.getConsoleView().setCursorGraphPosition(x, y);
        }
    }


    @PascalMethod(description = "graph library")
    public void ellipse(int x, int y, int start, int end, int rx, int ry) {
        if (handler.getConsoleView() != null) {

            mGraphScreen.addGraphObject(new ArcEllipseObject(x, y, start, end, rx, ry));
        }
    }


    @PascalMethod(description = "graph library")
    public void fillEllipse(int x, int y, int rx, int ry) {
        if (handler.getConsoleView() != null) {

            mGraphScreen.addGraphObject(new EllipseObject(x, y, rx, ry));
        }
    }


    @PascalMethod(description = "Return maximum number of colors")
    public int getMaxColor() {
        return 16777216;
    }


    @PascalMethod(description = "graph library")
    public void putPixel(int x, int y, int color) {
        if (handler.getConsoleView() != null) {
            mGraphScreen.addGraphObject(new PixelObject(x, y,
                    ColorUtils.pascalColorToAndroidColor(color)));
        }
    }


    @PascalMethod(description = "graph library")
    public void setLineStyle(int style, int linePattern, int width) {
        if (handler.getConsoleView() != null) {

            GraphScreen graphScreen = handler.getConsoleView().getGraphScreen();
            graphScreen.setLineStyle(style);
            graphScreen.setLinePattern(linePattern);
            graphScreen.setLineWidth(width);
        }
    }


    @PascalMethod(description = "graph library")
    public void outTextXY(int x, int y, StringBuilder text) {
        if (handler.getConsoleView() != null) {


            mGraphScreen.addGraphObject(new TextGraphObject(text.toString(), x, y));
           /* //get current paint
            Paint textPaint = mGraphScreen.getTextPaint();
            //get width of text
            int width = (int) textPaint.measureText(text);
            //move cursor to the end of the text (bottom-right)
            CursorConsole cursorGraph = handler.getConsoleView().getCursorGraphic();
            handler.getConsoleView().setCursorGraphPosition(cursorGraph.getX(),
                    cursorGraph.getY() + width);*/
        }
    }


    @PascalMethod(description = "graph library")
    public void outText(String text) {
        ConsoleCursor cursorGraph = handler.getConsoleView().getCursorGraphic();
        mGraphScreen.addGraphObject(new TextGraphObject(text, cursorGraph.getX(),
                cursorGraph.getY()));
        //get current paint
        Paint textPaint = mGraphScreen.getTextPaint();
        //get width of text
        int width = (int) textPaint.measureText(text);
        //move cursor to the end of the text (bottom-right)
        handler.getConsoleView().setCursorGraphPosition(cursorGraph.getX(),
                cursorGraph.getY() + width);
    }


    @PascalMethod(description = "graph library")
    public void installUserFont(String path) {
        // TODO: 09-Apr-17
    }


    @PascalMethod(description = "graph library")
    public int getBkColor() {
        if (handler.getConsoleView() != null)
            return mGraphScreen.getBackgroundColor();
        else
            return 0;
    }


    @PascalMethod(description = "graph library")
    public void setBkColor(int color) {
        if (handler.getConsoleView() != null) {
            handler.getConsoleView().setGraphBackground(ColorUtils.pascalColorToAndroidColor(color));
        }
    }


    /**
     * Set the current graphic viewport to the rectangle define by then top-left (x1, y1) and then
     * bottom-right (x2, y2). If clip
     */
    @PascalMethod(description = "graph library")
    public void setViewPort(int x1, int y1, int x2, int y2, boolean clip) {
        if (handler.getConsoleView() != null) {

            mGraphScreen.setViewPort(x1, y1, x2, y2, clip);
        }
    }

    @PascalMethod(description = "Set text style")
    public synchronized void setTextStyle(int fontID, int direction, int size) {
        if (handler.getConsoleView() != null) {
            GraphScreen graphScreen = handler.getConsoleView().getGraphScreen();
            graphScreen.setTextSize(size);
            graphScreen.setTextDirection(direction);
            graphScreen.setFontID(fontID);
        }
    }


    @PascalMethod(description = "Set drawing fill style")
    public void setFillStyle(int pattern, int color) {
        if (handler.getConsoleView() != null) {
            GraphScreen graphScreen = handler.getConsoleView().getGraphScreen();
            graphScreen.setFillPattern(pattern);
            graphScreen.setFillColor(ColorUtils.pascalColorToAndroidColor(color));
        }
    }


    @PascalMethod(description = "graph library")
    public void setDirectVideo(boolean assess) {
        // TODO: 09-Apr-17
    }


    @PascalMethod(description = "Draw and fill a sector of an ellipse")
    public void sector(int x, int y, int start, int end, int rx, int ry) {
        if (handler.getConsoleView() != null) {
            mGraphScreen.addGraphObject(new SectorObject(x, y, start, end, rx, ry));
        }
    }

    /**
     * PieSlice draws and fills a sector of a circle with center (X,Y) and radius Radius,
     * starting at angle Start and ending at angle Stop.
     */
    @PascalMethod(description = "Draw a pie-slice")
    public void pieSlice(int x, int y, int start, int end, int radius) {
        if (handler.getConsoleView() != null) {

            mGraphScreen.addGraphObject(new PieSliceObject(x, y, start, end, radius));
        }
    }


    @PascalMethod(description = "graph library")
    public StringBuilder graphErrorMsg(int errorCode) {
        // TODO: 09-Apr-17
        return new StringBuilder();
    }


    @PascalMethod(description = "graph library")
    public void graphDefaults() {
        //// TODO: 09-Apr-17
    }


    @PascalMethod(description = "graph library")
    public void getFillPattern() {

    }

    /**
     * GetAspectRatio determines the effective resolution of the screen. The aspect ration can then
     * be calculated as Xasp/Yasp.
     */
    @PascalMethod(description = "Return screen resolution")
    public void getAspectRatio(PascalReference<Integer> x, PascalReference<Integer> y) {
        x.set(getMaxX());
        y.set(getMaxY());
    }


    @PascalMethod(description = "graph library")
    public int getGraphMode() {
        return 1;
    }


    @PascalMethod(description = "graph library")
    public StringBuilder getModeName(int mode) {
        return new StringBuilder("android_graphics");
    }


    @PascalMethod(description = "graph library")
    public int getMaxMode() {
        return 1;
    }


    @PascalMethod(description = "Set text placement style")
    public void setTextJustify(int horizontal, int vertical) {
        if (handler.getConsoleView() != null) {
            TextJustify textJustify = mGraphScreen.getTextJustify();
            textJustify.setHorizontal(horizontal);
            textJustify.setVertical(vertical);
        }
    }


    @PascalMethod(description = "graph library")
    public void Bar3D(int x1, int y1, int x2, int y2, int depth, boolean topOn) {
        if (handler.getConsoleView() != null) {
            handler.getConsoleView().getGraphScreen()
                    .addGraphObject(new Bar3DObject(x1, y1, x2, y2, depth, topOn));
        }
    }


    @PascalMethod(description = "graph library")
    public void FillEllipse(int x, int y, int rx, int ry) {
        DLog.d(TAG, "FillEllipse: ");
        if (handler.getConsoleView() != null) {
            handler.getConsoleView().getGraphScreen()
                    .addGraphObject(new FillEllipseObject(x, y, rx, ry));
        }
    }


    @PascalMethod(description = "graph library")
    public void FloodFill(int x, int y, int borderColorIndex) {
        DLog.d(TAG, "FloodFill: ");
        if (handler.getConsoleView() != null) {

            Bitmap graphBitmap = mGraphScreen.getGraphBitmap();
            Bitmap fillBitmap = mGraphScreen.getFillBitmap(Bitmap.Config.ARGB_8888);

            FloodFill floodFill = new FloodFill(graphBitmap, fillBitmap);
            floodFill.fill(x, y, Color.BLUE, ColorUtils.pascalColorToAndroidColor(borderColorIndex));

            int[] imagePixels = floodFill.getImagePixels();

            graphBitmap.setPixels(imagePixels.clone(), 0, graphBitmap.getWidth(), 0, 0, graphBitmap.getWidth(),
                    graphBitmap.getHeight());
            floodFill.gc();
        }
    }


    @PascalMethod(description = "graph library")
    public void SetTextJustify(int horizontal, int vertical) {
        if (handler.getConsoleView() != null) {

            GraphScreen graphScreen = handler.getConsoleView().getGraphScreen();
            TextPaint textPaint = graphScreen.getTextPaint();
            textPaint.setTextJustify(new TextJustify(horizontal, vertical));
        }
    }

    @PascalMethod(description = "Return size to store image")
    public long ImageSize(int x1, int y1, int x2, int y2) {
        return Math.abs((x2 - x1) * (y2 - y1));
    }

    @PascalMethod(description = "Return a copy of a screen area")
    @SuppressWarnings("unchecked")
    public void getImage(int x1, int y1, int x2, int y2, PascalReference pascalPointer) {
        GraphScreen graphScreen = handler.getConsoleView().getGraphScreen();
        Bitmap graphBitmap = graphScreen.getGraphBitmap();

        Bitmap crop = Bitmap.createBitmap(graphBitmap, x1, y1, (x2 - x1), (y2 - y1));
        pascalPointer.set(crop);
    }

    @PascalMethod(description = "Draw an in-memory image to the screen")
    public void putImage(int x1, int y1, PascalReference pascalPointer, int mode)
            throws RuntimePascalException {
        //get graph bitmap
        GraphScreen graphScreen = handler.getConsoleView().getGraphScreen();
        Bitmap graphBitmap = graphScreen.getGraphBitmap();

        //draw bitmap
        Canvas canvas = new Canvas(graphBitmap);
        Bitmap bitmap = (Bitmap) pascalPointer.get();
        canvas.drawBitmap(bitmap, x1, y1, null);

        //invailidate screen
        handler.getConsoleView().postInvalidate();
    }

    @PascalMethod(description = "Return coordinates of last drawn arc or ellipse.")
    public void getArcCoords(PascalReference<ContainsVariables> var) {
        ArrayList<VariableDeclaration> vars = new ArrayList<>();
        vars.add(new VariableDeclaration("x", BasicType.Integer, 1, null));
        vars.add(new VariableDeclaration("y", BasicType.Integer, 1, null));
        vars.add(new VariableDeclaration("xstart", BasicType.Integer, 1, null));
        vars.add(new VariableDeclaration("ystart", BasicType.Integer, 1, null));
        vars.add(new VariableDeclaration("xend", BasicType.Integer, 1, null));
        vars.add(new VariableDeclaration("yend", BasicType.Integer, 1, null));

        RecordValue recordValue = new RecordValue(vars);
        var.set(recordValue);
    }

    @PascalMethod(description = "Return height (in pixels) of the given string")
    public int TextHeight(String text) {
        TextPaint textPaint = mGraphScreen.getTextPaint();
        Rect rect = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }

    @PascalMethod(description = "Return width (in pixels) of the given string")
    public int TextWidth(String text) {
        TextPaint textPaint = mGraphScreen.getTextPaint();
        Rect rect = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), rect);
        return rect.width();
    }

    @PascalMethod(description = "Restore text screen")
    public void RestoreCrtMode() {
        ConsoleView consoleView = handler.getConsoleView();
        if (consoleView != null) {
            consoleView.setGraphicMode(false);
            consoleView.postInvalidate();
        }
    }

    @PascalMethod(description = "Set graphical mode")
    public void SetGraphMode(int mode) {
        ConsoleView consoleView = handler.getConsoleView();
        if (consoleView != null) {
            consoleView.setGraphicMode(true);
            consoleView.postInvalidate();
        }
    }

    // ignore
    @PascalMethod(description = "Return lowest and highest modus of current driver")
    public void GetModeRange(int graphMode, PascalReference<Integer> low, PascalReference<Integer> high) {
        low.set(-1);
        high.set(-1);
    }

    @PascalMethod(description = "Draw, close and fill a polygone")
    public void FillPoly(int numPoint, RecordValue[] points) {
        ConsoleView consoleView = handler.getConsoleView();
        if (consoleView != null) {
            consoleView.getGraphScreen().addGraphObject(new PolygonObject(points, numPoint));
            consoleView.postInvalidate();
        }
    }


    @PascalMethod(description = "By enabling flag (setting it to true), every graph's " +
            "drawing routines don't go directly to the screen, but goes to screen buffer instead. After " +
            "all the drawing of each \"frame\" has been done, we simply call the drawBuffer() procedure " +
            "to copy the screen buffer content into the screen.")
    public void setBufferEnable(boolean enable) {
        ConsoleView consoleView = handler.getConsoleView();
        if (consoleView != null) {
            consoleView.getGraphScreen().setBufferEnable(enable);
        }
    }

    @PascalMethod(description = "draw buffer into the screen")
    public void drawBuffer() {
        ConsoleView consoleView = handler.getConsoleView();
        if (consoleView != null) {
            consoleView.getGraphScreen().bufferToPrimary();
            consoleView.postInvalidate();
        }
    }

    @PascalMethod(description = "clear buffer")
    public void clearBuffer() {
        ConsoleView consoleView = handler.getConsoleView();
        if (consoleView != null) {
            consoleView.getGraphScreen().clearBufferBitmap();
        }
    }
}
