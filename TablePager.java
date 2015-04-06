package ru.widgets;

import ru.widgets.uibinder.HorizontalPanelUI;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

/**
 * Виджет представляет собой панель для управления отображения табличных данных.
 */
public class TablePager extends AbstractPager {

	/**
	 * Styles used by this widget.
	 */
	interface Style extends CssResource {
		String pnlContent();

		String btnControl();

		String pageOfPageSize();

		String image();

		String button();
	}

	/**
	 * A ClientBundle that provides images for this widget.
	 */
	interface Resources extends ClientBundle {

		int WIDTH_IMAGE = 16;

		@ImageOptions(width = WIDTH_IMAGE)
		@Source("ru/public/img/fastNext.png")
		ImageResource fastNext();

		@ImageOptions(width = WIDTH_IMAGE)
		@Source("ru/public/img/fastBack.png")
		ImageResource fastBack();

		@ImageOptions(width = WIDTH_IMAGE)
		@Source("ru/public/img/next.png")
		ImageResource next();

		@ImageOptions(width = WIDTH_IMAGE)
		@Source("ru/public/img/prev.png")
		ImageResource prev();

		@ImageOptions(width = WIDTH_IMAGE)
		@Source("ru/public/css/TablePager.css")
		Style tablePagerStyle();
	}

	private static final int DEFAULT_PAGE_COUNT = 10;
	private static final int RIGHT_OFFSET_MIN = 6;
	private static final int RIGHT_OFFSET_MAX = 7;

	private Image fastForward;
	private Image fastBack;
	private Image nextPage;
	private Image prevPage;
	private Label pageOfPageSize;
	private ListBox selectRowsPerPage;
	private HorizontalPanel pnlContent;
	private HorizontalPanelUI pnlButtons;
	private TablePager.Resources resources;
	private TablePager.Style style;
	private int offsetPage = 1;
	private int dataSize = 0;
	private Integer numberButton = 1;
	private Boolean isClear = true;

	/**
	 * @param countRowsPerPage - количество отображаемых строк на странице. По умолчанию - 10
	 */
	public TablePager(int countRowsPerPage) {
		this();
		setPageSize(countRowsPerPage);
	}

	/**
	 * Количество отображаемых строк на странице. По умолчанию - 10
	 */
	public TablePager() {
		pnlContent = new HorizontalPanel();
		initWidget(pnlContent);
		init();
	}

	private Resources getDefaultResources() {
		if (resources == null) {
			resources = GWT.create(Resources.class);
		}
		return resources;
	}

	private void init() {
		resources = getDefaultResources();
		style = resources.tablePagerStyle();
		style.ensureInjected();
		fastForward = new Image(resources.fastNext());
		fastForward.setTitle("Вернуться к последней странице");
		fastForward.addClickHandler(getFastForwardPageClickHandler());
		fastBack = new Image(resources.fastBack());
		fastBack.setTitle("Вернуться к первой странице");
		fastBack.addClickHandler(getFastBackPageClickHandler());
		nextPage = new Image(resources.next());
		nextPage.setTitle("Следующая страница");
		nextPage.addClickHandler(getNextPageClickHandler());
		prevPage = new Image(resources.prev());
		prevPage.setTitle("Предыдущая страница");
		prevPage.addClickHandler(getPreviousPageClickHandler());
		pageOfPageSize = new Label("Количество записей");
		selectRowsPerPage = new ListBox(false);
		selectRowsPerPage.setTitle("Количество записей на странице");
		selectRowsPerPage.addItem("10");
		selectRowsPerPage.addItem("20");
		selectRowsPerPage.addItem("50");
		selectRowsPerPage.addItem("100");
		selectRowsPerPage.addChangeHandler(getSelectRowsPerPageChangeHandler());
		configurePnlContent();

		// Disable the buttons by default.
		setDisplay(null);
	}

	/**
	 * Переназначение id для элементов виджета.
	 * 
	 * @param idPrefix id prefix
	 */
	public void setElementsIds(String idPrefix) {
		idPrefix = (idPrefix == null ? "" : (idPrefix + "_")) + "TblPgr";
		getElement().setId(idPrefix);
		pnlButtons.setElementsIds(idPrefix);
		idPrefix += "_";
		fastForward.getElement().setId(idPrefix + "FastForward");
		fastBack.getElement().setId(idPrefix + "FastBack");
		nextPage.getElement().setId(idPrefix + "NextPage");
		prevPage.getElement().setId(idPrefix + "PrevPage");
		pageOfPageSize.getElement().setId(idPrefix + "PageOfPageSize");
		selectRowsPerPage.getElement().setId(idPrefix + "SelectRowsPerPage");
		pnlContent.getElement().setId(idPrefix + "PnlContent");
	}

	private ClickHandler getFastForwardPageClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setEnableButtons(true);
				if (getPageCount() > DEFAULT_PAGE_COUNT) {
					offsetPage = getPageCount() - DEFAULT_PAGE_COUNT;
					for (int i = 0; i < pnlButtons.getWidgetCount(); i++) {
						Button buttonInPnl = (Button) pnlButtons.getWidget(i);
						buttonInPnl.setText((offsetPage + i + 1) + "");
						if ((offsetPage + i + 1) == getPageCount()) {
							buttonInPnl.setEnabled(false);
							numberButton = Integer.valueOf(buttonInPnl.getText());
						}
					}
				} else {
					Button buttonInPnl = (Button) pnlButtons.getWidget(pnlButtons.getWidgetCount() - 1);
					buttonInPnl.setEnabled(false);
					numberButton = Integer.valueOf(buttonInPnl.getText());
				}
				isClear = false;
				lastPage();
			}
		};
	}

	private ClickHandler getFastBackPageClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setEnableButtons(true);
				offsetPage = 1;
				isClear = false;
				for (int i = 0; i < pnlButtons.getWidgetCount(); i++) {
					Button buttonInPnl = (Button) pnlButtons.getWidget(i);
					buttonInPnl.setText((offsetPage + i) + "");
					if (i == 0) {
						buttonInPnl.setEnabled(false);
					}
				}
				numberButton = 1;
				firstPage();
			}
		};
	}

	private ClickHandler getNextPageClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Button buttonEvent = setEnableButtons(true);
				if (getPageCount() > DEFAULT_PAGE_COUNT) {
					offsetPage = Integer.valueOf(((Button) pnlButtons.getWidget(0)).getText());
				} else {
					offsetPage = 1;
				}
				isClear = false;
				for (int i = 0; i < pnlButtons.getWidgetCount(); i++) {
					Button buttonInPnl = (Button) pnlButtons.getWidget(i);
					if (Integer.valueOf(((Button) pnlButtons.getWidget(pnlButtons.getWidgetCount() - 1)).getText()) == getPageCount() || offsetPage == 1 && buttonEvent.getTabIndex() <= RIGHT_OFFSET_MIN) {
						buttonInPnl.setText((offsetPage + i) + "");
					} else {
						buttonInPnl.setText((offsetPage + i + 1) + "");
					}
					if (buttonInPnl.getText().equals(String.valueOf(getPage() + 2))) {
						buttonInPnl.setEnabled(false);
					}
				}
				if (Integer.valueOf(buttonEvent.getText()) == getPageCount() - 1) {
					numberButton = getPageCount();
					lastPage();
				} else {
					numberButton = Integer.valueOf(buttonEvent.getText()) + 1;
					nextPage();
				}
			}
		};
	}

	private ClickHandler getPreviousPageClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Button buttonEvent = setEnableButtons(true);
				if (getPageCount() > DEFAULT_PAGE_COUNT) {
					offsetPage = Integer.valueOf(((Button) pnlButtons.getWidget(0)).getText());
				} else {
					offsetPage = 1;
				}
				isClear = false;
				for (int i = 0; i < pnlButtons.getWidgetCount(); i++) {
					Button buttonInPnl = (Button) pnlButtons.getWidget(i);
					if (offsetPage == 1 && buttonEvent.getTabIndex() <= RIGHT_OFFSET_MIN) {
						buttonInPnl.setText((offsetPage + i) + "");
					} else {
						buttonInPnl.setText((offsetPage + i - 1) + "");
					}
					if (buttonInPnl.getText().equals(String.valueOf(getPage()))) {
						buttonInPnl.setEnabled(false);
						numberButton = Integer.valueOf(buttonInPnl.getText());
					}
				}
				previousPage();
			}
		};
	}

	private ChangeHandler getSelectRowsPerPageChangeHandler() {
		return new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				firstPage();
				isClear = true;
				setPageSize(Integer.valueOf(selectRowsPerPage.getItemText(selectRowsPerPage.getSelectedIndex())));
			}
		};
	}

	/**
	 * Отображает текст в формате: "Показано <число>-<число> из <число>".
	 * 
	 * @return - сформированная строка
	 */
	public String createText() {
		HasRows display = getDisplay();
		Range range = display.getVisibleRange();
		int pageStart = range.getStart() + 1;
		int pageSize = range.getLength();
		dataSize = this.dataSize != 0 ? this.dataSize : display.getRowCount();
		int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
		String text = "Показано ".concat(String.valueOf(pageStart)).concat(" - ").concat(String.valueOf(endIndex)).concat(" из ").concat(String.valueOf(dataSize));
		return text;
	}

	/**
	 * Создание массива кнопок, расположенных горизонтально в количесве, указанном в параметре.
	 * 
	 * @param count - количество кнопок
	 */
	protected void createButtons(int count, Boolean isClear) {
		if (isClear) {
			pnlButtons.clear();
			int position = 1;
			Button button;
			for (int i = 0; i < count; i++) {
				button = new Button(String.valueOf(i + 1));
				if (i == 0) {
					button.setEnabled(false);
				}
				button.setStyleName(style.button());
				button.setTabIndex(position);
				pnlButtons.add(button);
				button.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						setEnableButtons(true);
						Button button = (Button) event.getSource();
						numberButton = Integer.valueOf(button.getText());
						int startNumberPage = Integer.valueOf(((Button) pnlButtons.getWidget(0)).getText());
						int endNumberPage = Integer.valueOf(((Button) pnlButtons.getWidget(pnlButtons.getWidgetCount() - 1)).getText());
						TablePager.this.isClear = false;
						int delta = 0;
						if (button.getTabIndex() >= RIGHT_OFFSET_MAX) {
							if (endNumberPage != getPageCount()) {
								delta = 1;
							}
							for (int i = 0; i < pnlButtons.getWidgetCount(); i++) {
								Button buttonInPnl = (Button) pnlButtons.getWidget(i);
								buttonInPnl.setText((startNumberPage + i + delta) + "");
								if ((startNumberPage + i + delta) == numberButton) {
									buttonInPnl.setEnabled(false);
								}
							}
						} else if (button.getTabIndex() <= RIGHT_OFFSET_MIN) {
							if (startNumberPage != 1) {
								delta = 1;
							}
							for (int i = 0; i < pnlButtons.getWidgetCount(); i++) {
								Button buttonInPnl = (Button) pnlButtons.getWidget(i);
								buttonInPnl.setText((startNumberPage + i - delta) + "");
								if ((startNumberPage + i - delta) == numberButton) {
									buttonInPnl.setEnabled(false);
								}
							}
						}
						setPage(numberButton - 1);
					}
				});
				position++;
			}
		}
	}

	@Override
	protected void onRangeOrRowCountChanged() {
		HasRows display = getDisplay();
		pageOfPageSize.setText(createText());
		if (display != null) {
			double dataSize = this.dataSize <= 1 ? 0 : display.getRowCount();
			double pageCount = dataSize / Double.valueOf(selectRowsPerPage.getValue(selectRowsPerPage.getSelectedIndex()));
			createButtons(pageCount < DEFAULT_PAGE_COUNT ? (pageCount - (int) pageCount == new Double(0) ? (int) pageCount : (int) pageCount + 1) : DEFAULT_PAGE_COUNT, isClear);

			if (!hasNextPage()) {
				fastForward.setVisible(false);
				nextPage.setVisible(false);
			} else {
				fastForward.setVisible(true);
				nextPage.setVisible(true);
			}

			if (!hasPreviousPage()) {
				fastBack.setVisible(false);
				prevPage.setVisible(false);
			} else {
				fastBack.setVisible(true);
				prevPage.setVisible(true);
			}
		}
	}

	public int getDataSize() {
		return dataSize;
	}

	/**
	 * Устанавливает общее количество значений.
	 * 
	 * @param dataSize - количество значений
	 */
	public void setDataSize(int dataSize) {
		this.dataSize = dataSize;
		onRangeOrRowCountChanged();
	}

	public Label getPageOfPageSize() {
		return pageOfPageSize;
	}

	public void setPageOfPageSize(Label pageOfPageSize) {
		this.pageOfPageSize = pageOfPageSize;
	}

	@Override
	public int getPage() {
		return super.getPage();
	}

	private Button setEnableButtons(boolean isEnable) {
		Button buttonEvent = null;
		for (int i = 0; i < pnlButtons.getWidgetCount(); i++) {
			Button button = (Button) pnlButtons.getWidget(i);
			if (!button.isEnabled()) {
				buttonEvent = button;
				button.setEnabled(isEnable);
			}
		}
		return buttonEvent;
	}

	private void configurePnlContent() {
		pnlButtons = new HorizontalPanelUI();
		pnlContent.add(pageOfPageSize);
		pnlContent.add(fastBack);
		pnlContent.add(prevPage);
		pnlContent.add(pnlButtons);
		pnlContent.add(nextPage);
		pnlContent.add(fastForward);
		pnlContent.add(selectRowsPerPage);
		pnlContent.setStyleName(style.pnlContent());
		pnlButtons.setStyleName(style.btnControl());
		pageOfPageSize.setStyleName(style.pageOfPageSize());
		fastBack.setStyleName(style.image());
		fastForward.setStyleName(style.image());
		nextPage.setStyleName(style.image());
		prevPage.setStyleName(style.image());
		pnlContent.setCellVerticalAlignment(pageOfPageSize, HasVerticalAlignment.ALIGN_MIDDLE);
		pnlContent.setCellVerticalAlignment(fastBack, HasVerticalAlignment.ALIGN_MIDDLE);
		pnlContent.setCellVerticalAlignment(fastForward, HasVerticalAlignment.ALIGN_MIDDLE);
		pnlContent.setCellVerticalAlignment(prevPage, HasVerticalAlignment.ALIGN_MIDDLE);
		pnlContent.setCellVerticalAlignment(nextPage, HasVerticalAlignment.ALIGN_MIDDLE);
		pnlContent.setCellVerticalAlignment(selectRowsPerPage, HasVerticalAlignment.ALIGN_MIDDLE);
		pnlContent.setCellVerticalAlignment(pnlButtons, HasVerticalAlignment.ALIGN_MIDDLE);
	}

	public Boolean getIsClear() {
		return isClear;
	}

	public void setIsClear(Boolean isClear) {
		this.isClear = isClear;
	}
}