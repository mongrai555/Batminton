package com.example.badminton.controller;

import com.example.badminton.model.*;
import com.example.badminton.service.DataManager;
import com.example.badminton.service.PricingService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller หลักสำหรับหน้าจอระบบจัดการสนามแบดมินตัน
 * รองรับการคิดเงิน จองสนาม เช่าอุปกรณ์ ซื้อสินค้า แนบสลิป
 * ระบบสมัครและจัดการสมาชิก และการแบ่งสิทธิ์ Role (Staff และ Admin)
 */
public class MainBillingController {

    // --- Main TabPane ---
    @FXML private TabPane mainTabPane;
    @FXML private Tab billingTab;
    @FXML private Tab memberTab;
    @FXML private Tab historyTab;
    @FXML private Tab adminTab;

    // --- Header & Role Management ---
    @FXML private ComboBox<User> activeUserComboBox;
    @FXML private Label userRoleBadgeLabel;
    @FXML private Label userPermissionLabel;

    // --- Tab 1: Billing & POS Controls ---
    @FXML private ComboBox<Member> memberComboBox;
    @FXML private Label memberInfoLabel;

    // Court Booking
    @FXML private CheckBox courtBookingCheckBox;
    @FXML private CheckBox usePackageHoursCheckBox;
    @FXML private ComboBox<Integer> courtNumberComboBox;
    @FXML private DatePicker bookingDatePicker;
    @FXML private ComboBox<String> timeSlotComboBox;
    @FXML private Spinner<Integer> hoursSpinner;
    @FXML private Label discountNoticeLabel;

    // Rental Equipment
    @FXML private CheckBox rentShoesCheckBox;
    @FXML private Spinner<Integer> shoesQtySpinner;
    @FXML private CheckBox rentRacketCheckBox;
    @FXML private Spinner<Integer> racketQtySpinner;

    // Retail Merchandise
    @FXML private ComboBox<ProductSaleItem> productComboBox;
    @FXML private Spinner<Integer> productQtySpinner;
    @FXML private Button addProductButton;

    // Bill Table
    @FXML private TableView<Billable> billItemTableView;
    @FXML private TableColumn<Billable, String> colType;
    @FXML private TableColumn<Billable, String> colName;
    @FXML private TableColumn<Billable, Integer> colQty;
    @FXML private TableColumn<Billable, Double> colUnitPrice;
    @FXML private TableColumn<Billable, Double> colTotal;
    @FXML private TableColumn<Billable, String> colDetails;

    // Slip Attachment
    @FXML private Button attachSlipButton;
    @FXML private Label slipPathLabel;
    @FXML private ImageView slipPreviewImageView;

    // Summary Labels
    @FXML private Label courtSubtotalLabel;
    @FXML private Label rentalSubtotalLabel;
    @FXML private Label productSubtotalLabel;
    @FXML private Label discountAmountLabel;
    @FXML private Label grandTotalLabel;

    // Actions
    @FXML private Button confirmCheckoutButton;
    @FXML private Button clearFormButton;

    // --- Tab 2: Membership Management ---
    @FXML private TextField regMemberIdTextField;
    @FXML private TextField regMemberNameTextField;
    @FXML private TextField regMemberPhoneTextField;
    @FXML private CheckBox regBuyPackageCheckBox;
    @FXML private TextField searchMemberTextField;

    @FXML private TableView<Member> memberTableView;
    @FXML private TableColumn<Member, String> colMemberId;
    @FXML private TableColumn<Member, String> colMemberName;
    @FXML private TableColumn<Member, String> colMemberPhone;
    @FXML private TableColumn<Member, Integer> colMemberAccumulated;
    @FXML private TableColumn<Member, Integer> colMemberPackage;
    @FXML private TableColumn<Member, String> colMemberRegDate;
    @FXML private TableColumn<Member, String> colMemberDiscountStatus;

    @FXML private Button editMemberButton;
    @FXML private Button deleteMemberButton;
    @FXML private Button topUpPackageButton;
    @FXML private Button selectMemberForBillingButton;

    // --- Tab 3: Bill History ---
    @FXML private TextField searchBillTextField;
    @FXML private TableView<Bill> historyTableView;
    @FXML private TableColumn<Bill, String> colHistoryBillId;
    @FXML private TableColumn<Bill, String> colHistoryDate;
    @FXML private TableColumn<Bill, String> colHistoryMember;
    @FXML private TableColumn<Bill, Double> colHistoryTotal;
    @FXML private TableColumn<Bill, String> colHistorySlip;
    @FXML private TextArea receiptPreviewTextArea;

    // --- Tab 4: Admin Revenue Summary ---
    @FXML private VBox adminReportContainer;
    @FXML private Label adminAccessDeniedLabel;
    @FXML private DatePicker revenueDatePicker;
    @FXML private Label dailyRevenueLabel;
    @FXML private Label monthlyRevenueLabel;
    @FXML private Label totalRevenueLabel;

    // Internal State
    private final DataManager dataManager = DataManager.getInstance();
    private final ObservableList<Billable> currentBillItems = FXCollections.observableArrayList();
    private final ObservableList<Member> memberObservableList = FXCollections.observableArrayList();
    private final ObservableList<Bill> historyList = FXCollections.observableArrayList();
    private String selectedSlipFilePath = null;
    private static final AtomicInteger billCounter = new AtomicInteger(1001);

    @FXML
    public void initialize() {
        setupRoleAndHeader();
        setupMemberSelection();
        setupCourtBookingControls();
        setupRentalControls();
        setupProductControls();
        setupBillTable();
        setupMembershipManagementTab();
        setupHistoryTab();
        setupAdminTab();

        recalculateLiveBill();
        refreshNextMemberId();
    }

    // ==========================================
    // 1. Role & Header System
    // ==========================================
    private void setupRoleAndHeader() {
        ObservableList<User> users = FXCollections.observableArrayList(dataManager.getSystemUsers());
        activeUserComboBox.setItems(users);
        activeUserComboBox.setValue(dataManager.getCurrentUser());

        activeUserComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                dataManager.setCurrentUser(newVal);
                updateRoleUiState();
            }
        });

        updateRoleUiState();
    }

    private void updateRoleUiState() {
        User current = dataManager.getCurrentUser();
        if (current == null) return;

        if (current instanceof Admin) {
            userRoleBadgeLabel.setText("[ ADMIN ]");
            userRoleBadgeLabel.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-padding: 3 8 3 8; -fx-background-radius: 4; -fx-font-weight: bold;");
            userPermissionLabel.setText("สิทธิ์: ผู้ดูแลระบบสูงสุด (จัดการ/ลบสมาชิกได้, ดูรายงานรายได้ทั้งหมด)");
            
            // ปลดล็อคฟีเจอร์สำหรับ Admin
            deleteMemberButton.setDisable(false);
            if (adminReportContainer != null) adminReportContainer.setVisible(true);
            if (adminAccessDeniedLabel != null) adminAccessDeniedLabel.setVisible(false);
            refreshRevenueKpis();
        } else {
            userRoleBadgeLabel.setText("[ STAFF ]");
            userRoleBadgeLabel.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-padding: 3 8 3 8; -fx-background-radius: 4; -fx-font-weight: bold;");
            userPermissionLabel.setText("สิทธิ์: พนักงานแคชเชียร์ (สมัครสมาชิก, บันทึกการจอง, ออกบิล)");

            // จำกัดสิทธิ์ของ Staff
            deleteMemberButton.setDisable(false); // ให้กดได้ แต่จะแจ้งเตือน SecurityException ตาม OOP Rule
            if (adminReportContainer != null) adminReportContainer.setVisible(false);
            if (adminAccessDeniedLabel != null) adminAccessDeniedLabel.setVisible(true);
        }
    }

    // ==========================================
    // 2. Billing & POS Tab
    // ==========================================
    private void setupMemberSelection() {
        refreshMemberListData();
        memberComboBox.setItems(memberObservableList);
        memberComboBox.getSelectionModel().selectFirst();
        updateMemberInfo();

        memberComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateMemberInfo();
            recalculateLiveBill();
        });
    }

    private void refreshMemberListData() {
        memberObservableList.setAll(dataManager.getMembers());
    }

    private void updateMemberInfo() {
        Member m = memberComboBox.getValue();
        if (m != null) {
            int pkgHours = m.getRemainingPackageHours();
            memberInfoLabel.setText(String.format("รหัส: %s | โทร: %s | สะสมแล้ว: %d ชม. (คงเหลือแพ็กเกจ: %d ชม.)",
                    m.getId(), m.getPhone(), m.getAccumulatedHours(), pkgHours));
            if (pkgHours > 0) {
                usePackageHoursCheckBox.setDisable(false);
                usePackageHoursCheckBox.setText(String.format("🎟️ ใช้สิทธิ์หักชั่วโมงจากแพ็กเกจเหมาจ่าย (โควตาคงเหลือ: %d ชม.)", pkgHours));
            } else {
                usePackageHoursCheckBox.setSelected(false);
                usePackageHoursCheckBox.setDisable(true);
                usePackageHoursCheckBox.setText("🎟️ ใช้สิทธิ์แพ็กเกจเหมาจ่าย (ไม่มีโควตาคงเหลือ)");
            }
        } else {
            memberInfoLabel.setText("ลูกค้าทั่วไป (ไม่มีประวัติสมาชิก - ซื้อเฉพาะสินค้า)");
            usePackageHoursCheckBox.setSelected(false);
            usePackageHoursCheckBox.setDisable(true);
            usePackageHoursCheckBox.setText("🎟️ ใช้สิทธิ์แพ็กเกจเหมาจ่าย (เฉพาะสมาชิก)");
        }
    }

    private void setupCourtBookingControls() {
        ObservableList<Integer> courts = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) courts.add(i);
        courtNumberComboBox.setItems(courts);
        courtNumberComboBox.getSelectionModel().selectFirst();

        bookingDatePicker.setValue(LocalDate.now());
        bookingDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateTimeSlotsByDate(newVal));
        updateTimeSlotsByDate(bookingDatePicker.getValue());

        hoursSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 15, 1));
        hoursSpinner.valueProperty().addListener((obs, oldVal, newVal) -> recalculateLiveBill());
        usePackageHoursCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> recalculateLiveBill());

        courtBookingCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            courtNumberComboBox.setDisable(!newVal);
            bookingDatePicker.setDisable(!newVal);
            timeSlotComboBox.setDisable(!newVal);
            hoursSpinner.setDisable(!newVal);
            usePackageHoursCheckBox.setDisable(!newVal || (memberComboBox.getValue() != null && memberComboBox.getValue().getRemainingPackageHours() == 0));
            recalculateLiveBill();
        });
    }

    private void updateTimeSlotsByDate(LocalDate date) {
        if (date == null) return;
        boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
        ObservableList<String> slots = FXCollections.observableArrayList();

        if (isWeekend) {
            // วันเสาร์-อาทิตย์: 10.00 - 20.00 น.
            for (int h = 10; h < 20; h++) {
                slots.add(String.format("%02d:00 - %02d:00 น.", h, h + 1));
            }
        } else {
            // วันธรรมดา: 13.00 - 21.00 น.
            for (int h = 13; h < 21; h++) {
                slots.add(String.format("%02d:00 - %02d:00 น.", h, h + 1));
            }
        }
        timeSlotComboBox.setItems(slots);
        timeSlotComboBox.getSelectionModel().selectFirst();
    }

    private void setupRentalControls() {
        shoesQtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));
        racketQtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));

        shoesQtySpinner.setDisable(true);
        racketQtySpinner.setDisable(true);

        rentShoesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            shoesQtySpinner.setDisable(!newVal);
            if (!newVal) shoesQtySpinner.getValueFactory().setValue(0);
            else if (shoesQtySpinner.getValue() == 0) shoesQtySpinner.getValueFactory().setValue(1);
            recalculateLiveBill();
        });

        rentRacketCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            racketQtySpinner.setDisable(!newVal);
            if (!newVal) racketQtySpinner.getValueFactory().setValue(0);
            else if (racketQtySpinner.getValue() == 0) racketQtySpinner.getValueFactory().setValue(1);
            recalculateLiveBill();
        });

        shoesQtySpinner.valueProperty().addListener((obs, oldVal, newVal) -> recalculateLiveBill());
        racketQtySpinner.valueProperty().addListener((obs, oldVal, newVal) -> recalculateLiveBill());
    }

    private void setupProductControls() {
        ObservableList<ProductSaleItem> products = FXCollections.observableArrayList(dataManager.getCatalogProducts());
        productComboBox.setItems(products);
        productComboBox.getSelectionModel().selectFirst();

        productQtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
        addProductButton.setOnAction(e -> onAddProduct());
    }

    private void setupBillTable() {
        colType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getItemType()));
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getItemName()));
        colQty.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getQuantity()).asObject());
        colUnitPrice.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getUnitPrice()).asObject());
        colTotal.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().calculateTotal()).asObject());
        colDetails.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDetails()));

        billItemTableView.setItems(currentBillItems);
    }

    public void recalculateLiveBill() {
        ObservableList<Billable> manualProducts = FXCollections.observableArrayList();
        for (Billable b : currentBillItems) {
            if (b instanceof ProductSaleItem) {
                manualProducts.add(b);
            }
        }

        currentBillItems.clear();

        Member currentMember = memberComboBox.getValue();
        int prevHours = (currentMember != null) ? currentMember.getAccumulatedHours() : 0;

        // 1. เช่าสนาม
        double courtTotal = 0.0;
        double discount20th = 0.0;
        boolean discountApplied = false;

        if (courtBookingCheckBox.isSelected()) {
            int courtNum = courtNumberComboBox.getValue() != null ? courtNumberComboBox.getValue() : 1;
            int hours = hoursSpinner.getValue() != null ? hoursSpinner.getValue() : 1;
            String dateStr = bookingDatePicker.getValue() != null ?
                    bookingDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            String slotStr = timeSlotComboBox.getValue() != null ? timeSlotComboBox.getValue() : "";

            int packageHoursUsed = 0;
            int paidHours = hours;

            if (usePackageHoursCheckBox.isSelected() && currentMember != null) {
                packageHoursUsed = Math.min(currentMember.getRemainingPackageHours(), hours);
                paidHours = hours - packageHoursUsed;
            }

            discount20th = PricingService.calculateTwentiethHourDiscount(prevHours, paidHours);
            if (discount20th > 0) {
                discountApplied = true;
                discountNoticeLabel.setText(String.format(
                        "🎉 สมาชิกสะสมครบ 19 ชม. แล้ว! ชั่วโมงที่ 20 ได้รับส่วนลด 30%% (-%.2f บาท)", discount20th));
                discountNoticeLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
            } else if (packageHoursUsed > 0 && paidHours == 0) {
                discountNoticeLabel.setText(String.format("🎟️ ใช้โควตาแพ็กเกจเหมาจ่าย %d ชม. (ฟรี ไม่มีค่าใช้จ่ายค่าสนามในบิลนี้)", packageHoursUsed));
                discountNoticeLabel.setStyle("-fx-text-fill: #1d4ed8; -fx-font-weight: bold;");
            } else if (packageHoursUsed > 0 && paidHours > 0) {
                discountNoticeLabel.setText(String.format("🎟️ หักแพ็กเกจ %d ชม. (ฟรี) + ชำระปกติ %d ชม. (@ 180 บ.)", packageHoursUsed, paidHours));
                discountNoticeLabel.setStyle("-fx-text-fill: #1d4ed8; -fx-font-weight: bold;");
            } else {
                int needed = Math.max(0, 19 - prevHours);
                if (needed > 0) {
                    discountNoticeLabel.setText(String.format(
                            "สะสมแล้ว %d ชม. (ขาดอีก %d ชม. จะได้รับสิทธิ์ลด 30%% ในชั่วโมงที่ 20)", prevHours, needed));
                } else {
                    discountNoticeLabel.setText("สะสมเกิน 20 ชม. เรียบร้อยแล้ว");
                }
                discountNoticeLabel.setStyle("-fx-text-fill: #64748b;");
            }

            CourtRentalItem courtItem = new CourtRentalItem(
                    courtNum, dateStr, slotStr, hours, packageHoursUsed, PricingService.COURT_HOURLY_RATE, discount20th, discountApplied);
            currentBillItems.add(courtItem);
            courtTotal = courtItem.calculateTotal();
        } else {
            discountNoticeLabel.setText("ไม่ได้เลือกจองสนาม (ซื้อเฉพาะสินค้าหรือเช่าอุปกรณ์)");
            discountNoticeLabel.setStyle("-fx-text-fill: #64748b;");
        }

        // 2. เช่าอุปกรณ์
        double rentalTotal = 0.0;
        if (rentShoesCheckBox.isSelected() && shoesQtySpinner.getValue() > 0) {
            EquipmentRentalItem shoesItem = new EquipmentRentalItem("เช่ารองเท้าแบดมินตัน", shoesQtySpinner.getValue());
            currentBillItems.add(shoesItem);
            rentalTotal += shoesItem.calculateTotal();
        }

        if (rentRacketCheckBox.isSelected() && racketQtySpinner.getValue() > 0) {
            EquipmentRentalItem racketItem = new EquipmentRentalItem("เช่าไม้แบดมินตัน", racketQtySpinner.getValue());
            currentBillItems.add(racketItem);
            rentalTotal += racketItem.calculateTotal();
        }

        // 3. สินค้าที่ซื้อ
        double productTotal = 0.0;
        for (Billable item : manualProducts) {
            currentBillItems.add(item);
            productTotal += item.calculateTotal();
        }

        double grandTotal = courtTotal + rentalTotal + productTotal;

        courtSubtotalLabel.setText(String.format("%.2f บาท", courtTotal));
        rentalSubtotalLabel.setText(String.format("%.2f บาท", rentalTotal));
        productSubtotalLabel.setText(String.format("%.2f บาท", productTotal));
        discountAmountLabel.setText(String.format("-%.2f บาท", discount20th));
        grandTotalLabel.setText(String.format("%.2f บาท", grandTotal));
    }

    private void onAddProduct() {
        ProductSaleItem template = productComboBox.getValue();
        if (template == null) return;
        int qty = productQtySpinner.getValue();
        if (qty <= 0) return;

        boolean found = false;
        for (Billable b : currentBillItems) {
            if (b instanceof ProductSaleItem && b.getItemName().equals(template.getItemName())) {
                ((AbstractBillItem) b).setQuantity(b.getQuantity() + qty);
                found = true;
                break;
            }
        }
        if (!found) {
            ProductSaleItem newItem = new ProductSaleItem(template.getItemName(), template.getUnitPrice(), qty);
            currentBillItems.add(newItem);
        }

        billItemTableView.refresh();
        recalculateLiveBill();
    }

    @FXML
    public void onRemoveSelectedItem() {
        Billable selected = billItemTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("กรุณาเลือกรายการที่ต้องการลบ");
            return;
        }
        if (selected instanceof CourtRentalItem) {
            courtBookingCheckBox.setSelected(false);
        } else if (selected instanceof EquipmentRentalItem) {
            if (selected.getItemName().contains("รองเท้า")) {
                rentShoesCheckBox.setSelected(false);
            } else {
                rentRacketCheckBox.setSelected(false);
            }
        } else {
            currentBillItems.remove(selected);
        }
        recalculateLiveBill();
    }

    @FXML
    public void onAttachSlip() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("เลือกไฟล์สลิปหลักฐานการโอนเงิน");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("รูปภาพสลิป (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("ทุกไฟล์ (*.*)", "*.*")
        );

        File file = fileChooser.showOpenDialog(attachSlipButton.getScene().getWindow());
        if (file != null) {
            selectedSlipFilePath = file.getAbsolutePath();
            slipPathLabel.setText(file.getName());
            try {
                Image image = new Image(file.toURI().toString(), 150, 150, true, true);
                slipPreviewImageView.setImage(image);
            } catch (Exception ex) {
                slipPathLabel.setText("แนบไฟล์แล้ว: " + file.getName());
            }
        }
    }

    @FXML
    public void onConfirmCheckout() {
        if (currentBillItems.isEmpty()) {
            showAlert("ไม่มีรายการสินค้าหรือการเช่าในบิล ไม่สามารถคิดเงินได้");
            return;
        }

        Member currentMember = memberComboBox.getValue();
        if (courtBookingCheckBox.isSelected() && currentMember == null) {
            showAlert("ตามกฎระเบียบ: ผู้ที่สามารถจองสนามแบดมินตันได้ ต้องสมัครเป็นสมาชิกเท่านั้น!");
            return;
        }

        String billId = "INV-" + billCounter.getAndIncrement();
        Bill bill = new Bill(billId, currentMember, dataManager.getCurrentUser());
        for (Billable item : currentBillItems) {
            bill.addItem(item);
        }
        bill.setSlipImagePath(selectedSlipFilePath);

        if (courtBookingCheckBox.isSelected() && currentMember != null) {
            int bookedHours = hoursSpinner.getValue();
            if (usePackageHoursCheckBox.isSelected()) {
                int pkgUsed = Math.min(currentMember.getRemainingPackageHours(), bookedHours);
                currentMember.deductPackageHours(pkgUsed);
            }
            currentMember.addAccumulatedHours(bookedHours);
            updateMemberInfo();
            memberTableView.refresh();
        }

        dataManager.saveBill(bill);
        historyList.setAll(dataManager.getBillHistory());
        refreshRevenueKpis();

        Alert receiptAlert = new Alert(Alert.AlertType.INFORMATION);
        receiptAlert.setTitle("บันทึกการชำระเงินและออกบิลสำเร็จ");
        receiptAlert.setHeaderText("ชำระเงินสำเร็จ: " + billId);

        TextArea ta = new TextArea(bill.generateReceiptText());
        ta.setEditable(false);
        ta.setPrefWidth(520);
        ta.setPrefHeight(450);
        ta.setStyle("-fx-font-family: 'Courier New', monospace;");

        receiptAlert.getDialogPane().setContent(ta);
        receiptAlert.showAndWait();

        onClearForm();
    }

    @FXML
    public void onClearForm() {
        courtBookingCheckBox.setSelected(true);
        usePackageHoursCheckBox.setSelected(false);
        hoursSpinner.getValueFactory().setValue(1);
        rentShoesCheckBox.setSelected(false);
        rentRacketCheckBox.setSelected(false);
        selectedSlipFilePath = null;
        slipPathLabel.setText("ยังไม่ได้แนบสลิป");
        slipPreviewImageView.setImage(null);
        currentBillItems.clear();
        recalculateLiveBill();
    }

    // ==========================================
    // 3. Membership Management Tab
    // ==========================================
    private void setupMembershipManagementTab() {
        colMemberId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        colMemberName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        colMemberPhone.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPhone()));
        colMemberAccumulated.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getAccumulatedHours()).asObject());
        colMemberPackage.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getRemainingPackageHours()).asObject());
        colMemberRegDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRegisteredDateFormatted()));
        colMemberDiscountStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDiscountStatusDescription()));

        memberTableView.setItems(memberObservableList);

        // Search live filter
        searchMemberTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            memberObservableList.setAll(dataManager.searchMembers(newVal));
        });
    }

    private void refreshNextMemberId() {
        if (regMemberIdTextField != null) {
            regMemberIdTextField.setText(dataManager.generateNextMemberId());
        }
    }

    @FXML
    public void onRegisterNewMember() {
        String id = regMemberIdTextField.getText().trim();
        String name = regMemberNameTextField.getText().trim();
        String phone = regMemberPhoneTextField.getText().trim();

        if (name.isEmpty()) {
            showAlert("กรุณาระบุชื่อ-นามสกุลของสมาชิก");
            return;
        }
        if (phone.isEmpty()) {
            showAlert("กรุณาระบุเบอร์โทรศัพท์ของสมาชิก");
            return;
        }

        try {
            Member newMember = new Member(id, name, phone, 0, LocalDate.now());
            if (regBuyPackageCheckBox.isSelected()) {
                newMember.addPackageHours(15);
                // บันทึกบิลซื้อแพ็กเกจเหมาจ่าย 15 ชั่วโมง 2,600 บาท
                Bill packageBill = new Bill("INV-PKG-" + billCounter.getAndIncrement(), newMember, dataManager.getCurrentUser());
                packageBill.addItem(new ProductSaleItem("แพ็กเกจเหมาจ่าย 15 ชั่วโมง", PricingService.PACKAGE_15_HOURS_PRICE, 1));
                dataManager.saveBill(packageBill);
                historyList.setAll(dataManager.getBillHistory());
            }

            dataManager.addMember(newMember);
            refreshMemberListData();
            memberComboBox.getSelectionModel().select(newMember);

            showSuccessAlert("สมัครสมาชิกสำเร็จ!\nรหัสสมาชิก: " + newMember.getId() + "\nชื่อ: " + newMember.getName() +
                    (regBuyPackageCheckBox.isSelected() ? "\n(ซื้อแพ็กเกจ 15 ชม. เรียบร้อย)" : ""));

            onClearRegistrationForm();
            refreshNextMemberId();
        } catch (Exception ex) {
            showAlert("เกิดข้อผิดพลาดในการสมัครสมาชิก: " + ex.getMessage());
        }
    }

    @FXML
    public void onClearRegistrationForm() {
        regMemberNameTextField.clear();
        regMemberPhoneTextField.clear();
        regBuyPackageCheckBox.setSelected(false);
    }

    @FXML
    public void onEditSelectedMember() {
        Member selected = memberTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("กรุณาเลือกสมาชิกที่ต้องการแก้ไขในตาราง");
            return;
        }

        TextInputDialog nameDialog = new TextInputDialog(selected.getName());
        nameDialog.setTitle("แก้ไขข้อมูลสมาชิก");
        nameDialog.setHeaderText("แก้ไขชื่อ-นามสกุลสำหรับรหัส " + selected.getId());
        nameDialog.setContentText("ชื่อ-นามสกุล:");
        Optional<String> nameResult = nameDialog.showAndWait();

        if (nameResult.isPresent() && !nameResult.get().trim().isEmpty()) {
            TextInputDialog phoneDialog = new TextInputDialog(selected.getPhone());
            phoneDialog.setTitle("แก้ไขข้อมูลสมาชิก");
            phoneDialog.setHeaderText("แก้ไขเบอร์โทรศัพท์สำหรับรหัส " + selected.getId());
            phoneDialog.setContentText("เบอร์โทรศัพท์:");
            Optional<String> phoneResult = phoneDialog.showAndWait();

            if (phoneResult.isPresent() && !phoneResult.get().trim().isEmpty()) {
                dataManager.updateMember(selected.getId(), nameResult.get().trim(), phoneResult.get().trim());
                memberTableView.refresh();
                updateMemberInfo();
                showSuccessAlert("บันทึกการแก้ไขข้อมูลสมาชิกสำเร็จ!");
            }
        }
    }

    @FXML
    public void onDeleteSelectedMember() {
        Member selected = memberTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("กรุณาเลือกสมาชิกที่ต้องการลบในตาราง");
            return;
        }

        User operator = dataManager.getCurrentUser();

        // ตรวจสอบสิทธิ์ทาง OOP (หากเป็น Staff จะโยน SecurityException หรือแจ้งเตือนตามบทบาท)
        if (!operator.canDeleteMembers()) {
            showAlert("⛔ สิทธิ์ไม่เพียงพอ!\nบทบาทปัจจุบันของคุณคือ [" + operator.getRole() + "]\nการลบสมาชิกต้องทำโดยผู้ดูแลระบบ [ADMIN] เท่านั้น");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("ยืนยันการลบสมาชิก");
        confirm.setHeaderText("คุณแน่ใจหรือไม่ที่จะลบสมาชิก: " + selected.getName() + " (" + selected.getId() + ")?");
        confirm.setContentText("การลบจะไม่สามารถกู้คืนข้อมูลได้");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dataManager.deleteMember(selected.getId(), operator);
                refreshMemberListData();
                showSuccessAlert("ลบสมาชิกเรียบร้อยแล้ว");
            } catch (SecurityException ex) {
                showAlert(ex.getMessage());
            }
        }
    }

    @FXML
    public void onTopUpPackageForSelectedMember() {
        Member selected = memberTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("กรุณาเลือกสมาชิกที่ต้องการซื้อแพ็กเกจ");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("ซื้อแพ็กเกจเหมาจ่าย 15 ชั่วโมง");
        confirm.setHeaderText("สมาชิก: " + selected.getName() + " (" + selected.getId() + ")");
        confirm.setContentText("ราคาแพ็กเกจ: 2,600 บาท (ได้ชั่วโมงเล่น 15 ชม.)\nต้องการดำเนินการต่อหรือไม่?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            selected.addPackageHours(15);
            Bill packageBill = new Bill("INV-PKG-" + billCounter.getAndIncrement(), selected, dataManager.getCurrentUser());
            packageBill.addItem(new ProductSaleItem("แพ็กเกจเหมาจ่าย 15 ชั่วโมง", PricingService.PACKAGE_15_HOURS_PRICE, 1));
            dataManager.saveBill(packageBill);
            historyList.setAll(dataManager.getBillHistory());
            memberTableView.refresh();
            refreshRevenueKpis();
            updateMemberInfo();

            showSuccessAlert("เติมแพ็กเกจ 15 ชม. ให้ " + selected.getName() + " สำเร็จ! (ออกบิล 2,600 บาท)");
        }
    }

    @FXML
    public void onSelectMemberForBilling() {
        Member selected = memberTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("กรุณาเลือกสมาชิกในตาราง");
            return;
        }
        memberComboBox.setValue(selected);
        mainTabPane.getSelectionModel().select(billingTab);
    }

    // ==========================================
    // 4. Bill History & Admin Tabs
    // ==========================================
    private void setupHistoryTab() {
        colHistoryBillId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBillId()));
        colHistoryDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedDate()));
        colHistoryMember.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getMember() != null ? cell.getValue().getMember().getName() : "ทั่วไป"));
        colHistoryTotal.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().calculateGrandTotal()).asObject());
        colHistorySlip.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().hasSlip() ? "แนบแล้ว ✓" : "-"));

        historyList.setAll(dataManager.getBillHistory());
        historyTableView.setItems(historyList);

        historyTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                receiptPreviewTextArea.setText(newVal.generateReceiptText());
            }
        });
    }

    private void setupAdminTab() {
        revenueDatePicker.setValue(LocalDate.now());
        revenueDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> refreshRevenueKpis());
        refreshRevenueKpis();
    }

    private void refreshRevenueKpis() {
        LocalDate date = revenueDatePicker.getValue();
        if (date != null) {
            double daily = dataManager.calculateRevenueByDate(date);
            double monthly = dataManager.calculateRevenueByMonth(date.getYear(), date.getMonthValue());
            double total = dataManager.calculateTotalRevenue();

            dailyRevenueLabel.setText(String.format("%.2f บาท", daily));
            monthlyRevenueLabel.setText(String.format("%.2f บาท", monthly));
            totalRevenueLabel.setText(String.format("%.2f บาท", total));
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("แจ้งเตือน");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("สำเร็จ");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
