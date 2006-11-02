// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.designer.mapper.ui.dnd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.ws.WindowSystem;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.UIManager;
import org.talend.designer.mapper.ui.color.ColorInfo;
import org.talend.designer.mapper.ui.color.ColorProviderMapper;
import org.talend.designer.mapper.ui.visualmap.table.DataMapTableView;
import org.talend.designer.mapper.ui.visualmap.zone.scrollable.TablesZoneView;

/**
 * Only one instance by ZoneView should be necessary. This class create 2 arrows which follow cursor when dragging on a
 * <code>DataMapTableView</code>. Arrows are adjusted at left and right on the limit between two
 * <code>TableItem</code>.
 * 
 * 
 * $Id$
 * 
 */
public class InsertionIndicator {

    private TablesZoneView tablesZoneViewParent;

    private Composite leftArrowDraggingIndicator;

    private Composite rightArrowDraggingIndicator;

    private MapperManager mapperManager;

    /**
     * current y of the indicator.
     */
    private int indicYPositionRefTable;

    private Listener tablePaintListener;

    private Table draggableTable;

    public static final int HEIGHT_INDICATOR = 10;

    /**
     * last y position of indicator calculated, where origin is origin of TablesZoneView.
     */
    private int lastIndicYPositionRefZone;

    private boolean visible;

    private final Color colorIndicator = ColorProviderMapper.getColor(ColorInfo.COLOR_DRAGGING_INSERTION_INDICATOR);

    private int lastNPixelsCovered;

    /**
     * 
     * 
     * @param tablesZoneViewParent
     * @param mapperManager
     */
    public InsertionIndicator(TablesZoneView tablesZoneViewParent, MapperManager mapperManager) {
        this.tablesZoneViewParent = tablesZoneViewParent;
        this.mapperManager = mapperManager;
        createComponents();
    }

    /**
     * DOC amaumont Comment method "createComponents".
     */
    private void createComponents() {

        leftArrowDraggingIndicator = new Composite(tablesZoneViewParent, SWT.NONE | SWT.NO_BACKGROUND);
        leftArrowDraggingIndicator.setVisible(false);
        leftArrowDraggingIndicator.setBackgroundMode(SWT.INHERIT_DEFAULT);
        FormData formDataLeftArrow = new FormData();
        formDataLeftArrow.width = 12;
        formDataLeftArrow.height = HEIGHT_INDICATOR;
        formDataLeftArrow.left = new FormAttachment(0, 0);
        formDataLeftArrow.left = new FormAttachment(0, -formDataLeftArrow.width + 2);
        formDataLeftArrow.top = new FormAttachment(0, 0);
        leftArrowDraggingIndicator.setLayoutData(formDataLeftArrow);

        rightArrowDraggingIndicator = new Composite(tablesZoneViewParent, SWT.NONE | SWT.NO_BACKGROUND);
        rightArrowDraggingIndicator.setVisible(false);
        rightArrowDraggingIndicator.setBackgroundMode(SWT.INHERIT_DEFAULT);
        FormData formDataRightArrow = new FormData();
        formDataRightArrow.width = 13;
        formDataRightArrow.height = HEIGHT_INDICATOR;
        formDataRightArrow.left = new FormAttachment(0, 0); // this property is recalculated at each top changement
        formDataRightArrow.top = new FormAttachment(0, 0);
        rightArrowDraggingIndicator.setLayoutData(formDataRightArrow);

        addListeners();
    }

    /**
     * DOC amaumont Comment method "addListeners".
     */
    private void addListeners() {
        Listener paintListener = new Listener() {

            public void handleEvent(Event event) {

                Composite composite = (Composite) event.widget;

                Rectangle bounds = composite.getBounds();

                // //////////////////////////////////////////////////////////
                // draw image filled with transparent pixels
                RGB transparentColor = new RGB(0, 255, 0);
                PaletteData paletteData = new PaletteData(new RGB[] { transparentColor });

                ImageData imageData = new ImageData(bounds.width, bounds.height, 1, paletteData);
                int transparentPixelValue = imageData.palette.getPixel(transparentColor);
                imageData.transparentPixel = transparentPixelValue;

                GC gc = event.gc;
                final Image image = new Image(gc.getDevice(), imageData);

                gc.drawImage(image, 0, 0);
                image.dispose();
                // ////////////////////////////////////////////////////////////////////////////

                // //////////////////////////////////////////////////////////
                // draw left arrow
                int yCenter = bounds.height / 2;
                int widthExternalArrow = 10;
                gc.setBackground(colorIndicator);
                gc.setForeground(colorIndicator);

                if (composite == leftArrowDraggingIndicator) {
                    // external left arrow
                    Point leftCrossPoint = new Point(widthExternalArrow, yCenter);
                    gc.fillPolygon(new int[] { 0, 0, leftCrossPoint.x, leftCrossPoint.y, 0, bounds.height, });
                    gc.drawLine(leftCrossPoint.x, leftCrossPoint.y, bounds.width, leftCrossPoint.y);

                } else {
                    // external right arrow
                    Point rightCrossPoint = new Point(bounds.width - widthExternalArrow, yCenter);
                    gc.fillPolygon(new int[] { bounds.width, 0, rightCrossPoint.x, rightCrossPoint.y, bounds.width,
                            bounds.height, });
                    gc.drawLine(rightCrossPoint.x, rightCrossPoint.y, -bounds.width, rightCrossPoint.y);
                }
            }
        };

        leftArrowDraggingIndicator.addListener(SWT.Paint, paintListener);
        rightArrowDraggingIndicator.addListener(SWT.Paint, paintListener);
    }

    /**
     * Update position of the indicator at top of <code>itemIndexTarget</code> position of the
     * <code>draggableTable</code>.
     * 
     * @param draggableTable
     * @param itemIndexTarget
     */
    public void updatePosition(Table draggableTable, int itemIndexTarget) {

        // System.out.println(itemIndexTarget);

        this.draggableTable = draggableTable;
        removeTablePaintListener();
        if (tablePaintListener == null) {

            tablePaintListener = new Listener() {

                public void handleEvent(Event event) {
                    drawIndicatorLineInTable(event);
                }

            };
        }
        FormLayout formLayout = (FormLayout) tablesZoneViewParent.getLayout();

        UIManager uiManager = mapperManager.getUiManager();
        TablesZoneView tablesZoneViewOutputs = uiManager.getTablesZoneViewOutputs();
        Display display = tablesZoneViewOutputs.getDisplay();
        Point tablePositionRefZone = display.map(draggableTable, tablesZoneViewParent, new Point(0, 0));

        FormData formDataLeftArrow = (FormData) leftArrowDraggingIndicator.getLayoutData();
        FormData formDataRightArrow = (FormData) rightArrowDraggingIndicator.getLayoutData();
        ScrollBar verticalBar = draggableTable.getVerticalBar();
        int offsetVerticalBar = -verticalBar.getSelection() * draggableTable.getItemHeight();
        int indicYPositionRefZone = 0;
        if (WindowSystem.isGTK()) {
            if (itemIndexTarget == 0) {
                indicYPositionRefTable = 0 + offsetVerticalBar;
            } else {
                indicYPositionRefTable = itemIndexTarget * (draggableTable.getItemHeight() + 2) + offsetVerticalBar;
            }
            indicYPositionRefZone = indicYPositionRefTable + tablePositionRefZone.y + formLayout.marginTop - HEIGHT_INDICATOR / 2;
            indicYPositionRefZone -= draggableTable.getItemHeight() - 3;
        } else {
            if (itemIndexTarget == 0) {
                indicYPositionRefTable = 0 + offsetVerticalBar;
            } else {
                indicYPositionRefTable = itemIndexTarget * draggableTable.getItemHeight() - 1 + offsetVerticalBar;
            }
            indicYPositionRefZone = indicYPositionRefTable + tablePositionRefZone.y + formLayout.marginTop - HEIGHT_INDICATOR / 2;
        }

        DataMapTableView dataMapTableView = mapperManager.retrieveDataMapTableView(draggableTable);
        Rectangle boundsTableView = dataMapTableView.getBounds();

        int testValue = boundsTableView.y + boundsTableView.height - formLayout.marginTop - HEIGHT_INDICATOR / 2 - 5;
        if (indicYPositionRefZone > testValue) {
            indicYPositionRefZone = testValue;
        }

        draggableTable.addListener(SWT.Paint, tablePaintListener);
        if (lastIndicYPositionRefZone != indicYPositionRefZone) {
            formDataLeftArrow.top.offset = indicYPositionRefZone;
            formDataRightArrow.top.offset = indicYPositionRefZone;

            formDataRightArrow.left.offset = draggableTable.getSize().x + 2;
            draggableTable.redraw();
            tablesZoneViewParent.layout();
        }
        lastIndicYPositionRefZone = indicYPositionRefZone;
    }

    /**
     * DOC amaumont Comment method "removeTablePaintListener".
     */
    private void removeTablePaintListener() {
        if (this.draggableTable != null && tablePaintListener != null) {
            draggableTable.removeListener(SWT.Paint, tablePaintListener);
        }
        tablePaintListener = null;
    }

    /**
     * Hide left and right arrows, and redraw current table without line indication.
     */
    private void hide() {
        removeTablePaintListener();
        if (draggableTable != null) {
            draggableTable.redraw();
            draggableTable.layout();
        }
        lastIndicYPositionRefZone = 0;
        leftArrowDraggingIndicator.setVisible(false);
        rightArrowDraggingIndicator.setVisible(false);

    }

    /**
     * Hide left and right arrows, and redraw current table without line indication.
     */
    private void show() {
        draggableTable.redraw();
        leftArrowDraggingIndicator.setVisible(true);
        rightArrowDraggingIndicator.setVisible(true);
    }

    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            if (visible) {
                show();
            } else {
                hide();
            }
        }
        this.visible = visible;
    }

    public boolean isVisible() {
        return this.visible;
    }

    private void drawIndicatorLineInTable(Event event) {
        if (visible) {
            GC gc = event.gc;
            Rectangle area = draggableTable.getClientArea();
            int y = 0;
            if (WindowSystem.isGTK()) {
                y = indicYPositionRefTable;
            } else {
                y = draggableTable.getHeaderHeight() + indicYPositionRefTable;
            }

            gc.setForeground(colorIndicator);
            gc.drawLine(0, y, area.width, y);
        }
    }

    public void redraw() {
        if (draggableTable != null) {
            draggableTable.redraw();
        }
    }

    /**
     * DOC amaumont Comment method "isIntersect".
     * 
     * @param boundsPopupFromMapperShellOrigin
     * @return
     */
    public boolean isLeftArrowMustBeRefreshed(Rectangle boundsPopupFromMapperShellOrigin) {
        Shell parentShell = tablesZoneViewParent.getShell();
        Display display = parentShell.getDisplay();
        Point positionFromMapperShellOrigin = display.map(leftArrowDraggingIndicator, parentShell, new Point(0, 0));
        Rectangle leftArrowDraggingIndicatorBounds = leftArrowDraggingIndicator.getBounds();
        Rectangle boundsFromPositionOrigin = new Rectangle(positionFromMapperShellOrigin.x - 4,
                positionFromMapperShellOrigin.y - 4, leftArrowDraggingIndicatorBounds.width + 8,
                leftArrowDraggingIndicatorBounds.height + 8);
        Rectangle rectangleIntersect = boundsFromPositionOrigin.intersection(boundsPopupFromMapperShellOrigin);
        int nPixelsCoveredByPopup = rectangleIntersect.width * rectangleIntersect.height;
        boolean response = false;
        if (nPixelsCoveredByPopup < this.lastNPixelsCovered) {
            response = true;
        }
        this.lastNPixelsCovered = nPixelsCoveredByPopup;
        return response;
    }

    public void setLefArrowVisible(boolean visible) {
        leftArrowDraggingIndicator.setVisible(visible);
    }

    public void setRightArrowVisible(boolean visible) {
        rightArrowDraggingIndicator.setVisible(visible);
    }

    public boolean isRightArrowVisible() {
        return rightArrowDraggingIndicator.getVisible();
    }

}
