import { useEffect, useState } from "react";

const API_BASE = "http://localhost:8080/api";

function App() {
  console.log("SMARTSTOCK APP LOADED");

  // ============================================================
  // MAIN STATE
  // ============================================================

  const [activeSection, setActiveSection] =
    useState("dashboard");

  const [products, setProducts] = useState([]);
  const [movements, setMovements] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [recommendations, setRecommendations] =
    useState([]);
  const [forecast, setForecast] = useState(null);

  const [suppliers, setSuppliers] = useState([]);

  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  // ============================================================
  // PRODUCT FORM
  // ============================================================

  const emptyProductForm = {
    name: "",
    sku: "",
    description: "",
    price: "",
    quantity: "",
    lowStockThreshold: "",
    reorderLevel: "",
    leadTimeDays: "",
    safetyStock: "",
    minimumOrderQuantity: "",
    maximumStockLevel: "",
    supplierId: "",
  };

  const [productForm, setProductForm] =
    useState(emptyProductForm);

  const [editingProductId, setEditingProductId] =
    useState(null);

  // ============================================================
  // STOCK MOVEMENT FORM
  // ============================================================

  const [movementForm, setMovementForm] = useState({
    productId: "",
    movementType: "PURCHASE",
    quantity: "",
    note: "",
  });

  // ============================================================
  // SUPPLIER FORM
  // ============================================================

  const emptySupplierForm = {
    name: "",
    contactPerson: "",
    email: "",
    phone: "",
    address: "",
    leadTimeDays: "",
    active: true,
  };

  const [supplierForm, setSupplierForm] =
    useState(emptySupplierForm);

  const [editingSupplierId, setEditingSupplierId] =
    useState(null);

  // ============================================================
  // RECOMMENDATION DETAILS
  // ============================================================

  const [selectedRecommendation, setSelectedRecommendation] =
    useState(null);

  // ============================================================
  // FETCH PRODUCTS
  // ============================================================

  async function fetchProducts() {
    const response = await fetch(
      `${API_BASE}/products`
    );

    if (!response.ok) {
      throw new Error("Failed to load products");
    }

    return response.json();
  }

  // ============================================================
  // FETCH STOCK MOVEMENTS
  // ============================================================

  async function fetchMovements() {
    const response = await fetch(
      `${API_BASE}/stock-movements`
    );

    if (!response.ok) {
      throw new Error(
        "Failed to load stock movements"
      );
    }

    return response.json();
  }

  // ============================================================
  // FETCH INVENTORY ALERTS
  // ============================================================

  async function fetchAlerts() {
    const response = await fetch(
      `${API_BASE}/products/inventory-alerts`
    );

    if (!response.ok) {
      throw new Error(
        "Failed to load inventory alerts"
      );
    }

    return response.json();
  }

  // ============================================================
  // FETCH REORDER RECOMMENDATIONS
  // ============================================================

  async function fetchRecommendations() {
    const response = await fetch(
      `${API_BASE}/products/reorder-recommendations`
    );

    if (!response.ok) {
      throw new Error(
        "Failed to load reorder recommendations"
      );
    }

    return response.json();
  }

  // ============================================================
  // FETCH SUPPLIERS
  // ============================================================

  async function fetchSuppliers() {
    const response = await fetch(
      `${API_BASE}/suppliers`
    );

    if (!response.ok) {
      throw new Error(
        "Failed to load suppliers"
      );
    }

    return response.json();
  }

  // ============================================================
  // FETCH DEMAND FORECAST
  // ============================================================

  async function fetchForecast(productId) {
    if (!productId) {
      setForecast(null);
      return;
    }

    try {
      const response = await fetch(
        `${API_BASE}/stock-movements/product/${productId}/forecast`
      );

      if (!response.ok) {
        setForecast(null);
        return;
      }

      const data = await response.json();

      setForecast(data);
    } catch (error) {
      console.error("Forecast error:", error);
      setForecast(null);
    }
  }

  // ============================================================
  // LOAD ALL DATA
  // ============================================================

  async function loadData() {
    try {
      setLoading(true);

      const [
        productsData,
        movementsData,
        alertsData,
        recommendationsData,
        suppliersData,
      ] = await Promise.all([
        fetchProducts(),
        fetchMovements(),
        fetchAlerts(),
        fetchRecommendations(),
        fetchSuppliers(),
      ]);

      setProducts(productsData);
      setMovements(movementsData);
      setAlerts(alertsData);
      setRecommendations(recommendationsData);
      setSuppliers(suppliersData);

      if (productsData.length > 0) {
        await fetchForecast(productsData[0].id);
      } else {
        setForecast(null);
      }

      setMessage("");
    } catch (error) {
      console.error(error);

      setMessage(
        "Unable to connect to the SmartStock backend. Make sure Spring Boot is running."
      );
    } finally {
      setLoading(false);
    }
  }

  // ============================================================
  // INITIAL LOAD
  // ============================================================

  useEffect(() => {
    loadData();
  }, []);

  // ============================================================
  // PRODUCT FORM HANDLER
  // ============================================================

  function handleProductChange(event) {
    const { name, value } = event.target;

    setProductForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  }

  // ============================================================
  // RESET PRODUCT FORM
  // ============================================================

  function resetProductForm() {
    setProductForm(emptyProductForm);
    setEditingProductId(null);
  }

  // ============================================================
  // EDIT PRODUCT
  // ============================================================

  function handleEditProduct(product) {
    setEditingProductId(product.id);

    setProductForm({
      name: product.name ?? "",
      sku: product.sku ?? "",
      description: product.description ?? "",
      price: product.price ?? "",
      quantity: product.quantity ?? "",
      lowStockThreshold:
        product.lowStockThreshold ?? "",
      reorderLevel:
        product.reorderLevel ?? "",
      leadTimeDays:
        product.leadTimeDays ?? "",
      safetyStock:
        product.safetyStock ?? "",
      minimumOrderQuantity:
        product.minimumOrderQuantity ?? "",
      maximumStockLevel:
        product.maximumStockLevel ?? "",
      supplierId:
        product.supplier?.id
          ? String(product.supplier.id)
          : "",
    });

    setActiveSection("products");

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });

    setMessage(
      `Editing product #${product.id}. Update the required values and save.`
    );
  }

  // ============================================================
  // ADD OR UPDATE PRODUCT
  // ============================================================

  async function handleProductSubmit(event) {
    event.preventDefault();

    try {
      setLoading(true);

      const product = {
        name: productForm.name,
        sku: productForm.sku,
        description: productForm.description,
        price: Number(productForm.price),
        quantity: Number(productForm.quantity),

        lowStockThreshold: Number(
          productForm.lowStockThreshold
        ),

        reorderLevel: Number(
          productForm.reorderLevel
        ),

        leadTimeDays: Number(
          productForm.leadTimeDays
        ),

        safetyStock: Number(
          productForm.safetyStock
        ),

        minimumOrderQuantity: Number(
          productForm.minimumOrderQuantity
        ),

        maximumStockLevel: Number(
          productForm.maximumStockLevel
        ),

        supplier: productForm.supplierId
          ? {
            id: Number(
              productForm.supplierId
            ),
          }
          : null,
      };

      // ========================================================
      // UPDATE EXISTING PRODUCT
      // ========================================================

      if (editingProductId !== null) {
        const response = await fetch(
          `${API_BASE}/products/${editingProductId}`,
          {
            method: "PUT",
            headers: {
              "Content-Type":
                "application/json",
            },
            body: JSON.stringify(product),
          }
        );

        if (!response.ok) {
          const errorText =
            await response.text();

          throw new Error(
            errorText ||
            "Failed to update product"
          );
        }

        resetProductForm();

        setMessage(
          "Product updated successfully."
        );

        await loadData();

        return;
      }

      // ========================================================
      // ADD NEW PRODUCT
      // ========================================================

      const response = await fetch(
        `${API_BASE}/products`,
        {
          method: "POST",
          headers: {
            "Content-Type":
              "application/json",
          },
          body: JSON.stringify(product),
        }
      );

      if (!response.ok) {
        const errorText =
          await response.text();

        throw new Error(
          errorText ||
          "Failed to add product"
        );
      }

      resetProductForm();

      setMessage(
        "Product added successfully."
      );

      await loadData();
    } catch (error) {
      console.error(error);

      setMessage(
        error.message ||
        "Failed to save product."
      );
    } finally {
      setLoading(false);
    }
  }

  // ============================================================
  // DELETE PRODUCT
  // ============================================================

  async function handleDeleteProduct(id) {
    const confirmed = window.confirm(
      "Are you sure you want to delete this product?"
    );

    if (!confirmed) {
      return;
    }

    try {
      setLoading(true);

      const response = await fetch(
        `${API_BASE}/products/${id}`,
        {
          method: "DELETE",
        }
      );

      if (!response.ok) {
        throw new Error(
          "Failed to delete product"
        );
      }

      if (editingProductId === id) {
        resetProductForm();
      }

      setMessage(
        "Product deleted successfully."
      );

      await loadData();
    } catch (error) {
      console.error(error);

      setMessage(
        "Failed to delete product."
      );
    } finally {
      setLoading(false);
    }
  }

  // ============================================================
  // MOVEMENT FORM HANDLER
  // ============================================================

  function handleMovementChange(event) {
    const { name, value } = event.target;

    setMovementForm((previous) => {
      let updatedValue = value;

      // --------------------------------------------------------
      // SALE QUANTITY LIMIT
      // --------------------------------------------------------

      if (name === "quantity") {
        const selectedProduct =
          products.find(
            (product) =>
              Number(product.id) ===
              Number(previous.productId)
          );

        if (
          selectedProduct &&
          previous.movementType === "SALE"
        ) {
          const availableStock =
            Number(
              selectedProduct.quantity || 0
            );

          const enteredQuantity =
            Number(value);

          if (
            Number.isFinite(
              enteredQuantity
            ) &&
            enteredQuantity >
            availableStock
          ) {
            updatedValue =
              String(availableStock);

            setMessage(
              `Maximum sale quantity for ${selectedProduct.name} is ${availableStock} units.`
            );
          }
        }
      }

      return {
        ...previous,
        [name]: updatedValue,
      };
    });

    // Clear validation message when
    // changing product/type.
    if (
      name === "productId" ||
      name === "movementType"
    ) {
      setMessage("");
    }
  }

  // ============================================================
  // GET SELECTED MOVEMENT PRODUCT
  // ============================================================

  function getSelectedMovementProduct() {
    if (!movementForm.productId) {
      return null;
    }

    return (
      products.find(
        (product) =>
          Number(product.id) ===
          Number(
            movementForm.productId
          )
      ) || null
    );
  }

  // ============================================================
  // GET AVAILABLE SALE STOCK
  // ============================================================

  function getAvailableSaleStock() {
    const selectedProduct =
      getSelectedMovementProduct();

    if (!selectedProduct) {
      return null;
    }

    return Number(
      selectedProduct.quantity || 0
    );
  }

  // ============================================================
  // CREATE STOCK MOVEMENT
  // ============================================================

  async function handleCreateMovement(event) {
    event.preventDefault();

    if (
      !movementForm.productId ||
      !movementForm.quantity
    ) {
      setMessage(
        "Please select a product and enter quantity."
      );

      return;
    }

    const selectedProduct =
      getSelectedMovementProduct();

    if (!selectedProduct) {
      setMessage(
        "Selected product could not be found."
      );

      return;
    }

    const quantity = Number(
      movementForm.quantity
    );

    // ==========================================================
    // QUANTITY VALIDATION
    // ==========================================================

    if (
      !Number.isInteger(quantity) ||
      quantity <= 0
    ) {
      setMessage(
        "Quantity must be a positive whole number."
      );

      return;
    }

    // ==========================================================
    // SALE STOCK VALIDATION
    // ==========================================================

    if (
      movementForm.movementType ===
      "SALE"
    ) {
      const currentStock =
        Number(
          selectedProduct.quantity || 0
        );

      if (quantity > currentStock) {
        setMessage(
          `Cannot record SALE of ${quantity} units. ${selectedProduct.name} currently has only ${currentStock} units in stock.`
        );

        return;
      }
    }

    try {
      setLoading(true);

      const movement = {
        productId: Number(
          movementForm.productId
        ),
        movementType:
          movementForm.movementType,
        quantity,
        movementDate:
          new Date().toISOString(),
        note: movementForm.note,
      };

      const response = await fetch(
        `${API_BASE}/stock-movements`,
        {
          method: "POST",
          headers: {
            "Content-Type":
              "application/json",
          },
          body: JSON.stringify(
            movement
          ),
        }
      );

      if (!response.ok) {
        const errorText =
          await response.text();

        let errorMessage =
          "Failed to create stock movement.";

        try {
          const errorData =
            JSON.parse(errorText);

          if (errorData.message) {
            errorMessage =
              errorData.message;
          } else if (errorData.error) {
            errorMessage =
              errorData.error;
          }
        } catch {
          if (errorText) {
            errorMessage =
              errorText;
          }
        }

        throw new Error(
          errorMessage
        );
      }

      const movementType =
        movementForm.movementType;

      setMovementForm({
        productId: "",
        movementType: "PURCHASE",
        quantity: "",
        note: "",
      });

      setMessage(
        `${movementType} transaction recorded successfully.`
      );

      await loadData();
    } catch (error) {
      console.error(error);

      setMessage(
        error.message ||
        "Failed to create stock movement."
      );
    } finally {
      setLoading(false);
    }
  }

  // ============================================================
  // SUPPLIER FORM HANDLER
  // ============================================================

  function handleSupplierChange(event) {
    const {
      name,
      value,
      type,
      checked,
    } = event.target;

    setSupplierForm((previous) => ({
      ...previous,
      [name]:
        type === "checkbox"
          ? checked
          : value,
    }));
  }

  // ============================================================
  // RESET SUPPLIER FORM
  // ============================================================

  function resetSupplierForm() {
    setSupplierForm(emptySupplierForm);
    setEditingSupplierId(null);
  }

  // ============================================================
  // EDIT SUPPLIER
  // ============================================================

  function handleEditSupplier(supplier) {
    setEditingSupplierId(
      supplier.id
    );

    setSupplierForm({
      name: supplier.name ?? "",
      contactPerson:
        supplier.contactPerson ?? "",
      email:
        supplier.email ?? "",
      phone:
        supplier.phone ?? "",
      address:
        supplier.address ?? "",
      leadTimeDays:
        supplier.leadTimeDays ?? "",
      active:
        supplier.active ?? true,
    });

    setActiveSection("suppliers");

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });

    setMessage(
      `Editing supplier #${supplier.id}. Update the required values and save.`
    );
  }

  // ============================================================
  // ADD OR UPDATE SUPPLIER
  // ============================================================

  async function handleSupplierSubmit(event) {
    event.preventDefault();

    try {
      setLoading(true);

      const supplier = {
        name: supplierForm.name,
        contactPerson:
          supplierForm.contactPerson,
        email: supplierForm.email,
        phone: supplierForm.phone,
        address: supplierForm.address,
        leadTimeDays: Number(
          supplierForm.leadTimeDays
        ),
        active:
          supplierForm.active,
      };

      if (editingSupplierId !== null) {
        const response = await fetch(
          `${API_BASE}/suppliers/${editingSupplierId}`,
          {
            method: "PUT",
            headers: {
              "Content-Type":
                "application/json",
            },
            body: JSON.stringify(
              supplier
            ),
          }
        );

        if (!response.ok) {
          const errorText =
            await response.text();

          throw new Error(
            errorText ||
            "Failed to update supplier"
          );
        }

        resetSupplierForm();

        setMessage(
          "Supplier updated successfully."
        );

        await loadData();

        return;
      }

      const response = await fetch(
        `${API_BASE}/suppliers`,
        {
          method: "POST",
          headers: {
            "Content-Type":
              "application/json",
          },
          body: JSON.stringify(
            supplier
          ),
        }
      );

      if (!response.ok) {
        const errorText =
          await response.text();

        throw new Error(
          errorText ||
          "Failed to add supplier"
        );
      }

      resetSupplierForm();

      setMessage(
        "Supplier added successfully."
      );

      await loadData();
    } catch (error) {
      console.error(error);

      setMessage(
        error.message ||
        "Failed to save supplier."
      );
    } finally {
      setLoading(false);
    }
  }

  // ============================================================
  // DELETE SUPPLIER
  // ============================================================

  async function handleDeleteSupplier(id) {
    const confirmed = window.confirm(
      "Are you sure you want to delete this supplier?"
    );

    if (!confirmed) {
      return;
    }

    try {
      setLoading(true);

      const response = await fetch(
        `${API_BASE}/suppliers/${id}`,
        {
          method: "DELETE",
        }
      );

      if (!response.ok) {
        const errorText =
          await response.text();

        throw new Error(
          errorText ||
          "Failed to delete supplier"
        );
      }

      if (editingSupplierId === id) {
        resetSupplierForm();
      }

      setMessage(
        "Supplier deleted successfully."
      );

      await loadData();
    } catch (error) {
      console.error(error);

      setMessage(
        error.message ||
        "Failed to delete supplier."
      );
    } finally {
      setLoading(false);
    }
  }

  // ============================================================
  // LOAD DEMAND FORECAST
  // ============================================================

  async function handleForecastChange(
    productId
  ) {
    await fetchForecast(productId);
  }

  // ============================================================
  // LOAD SMART RECOMMENDATION
  // ============================================================

  async function loadRecommendation(
    productId
  ) {
    try {
      const response = await fetch(
        `${API_BASE}/recommendations/product/${productId}`
      );

      if (!response.ok) {
        throw new Error(
          "Failed to load recommendation"
        );
      }

      const data =
        await response.json();

      setSelectedRecommendation(
        data
      );
    } catch (error) {
      console.error(error);

      setSelectedRecommendation(
        null
      );

      setMessage(
        "Unable to load recommendation."
      );
    }
  }

  // ============================================================
  // DASHBOARD CALCULATIONS
  // ============================================================

  const totalProducts =
    products.length;

  const totalStock =
    products.reduce(
      (sum, product) =>
        sum +
        (product.quantity
          ? Number(
            product.quantity
          )
          : 0),
      0
    );

  const lowStockCount =
    alerts.filter(
      (alert) =>
        alert.severity === "HIGH" ||
        alert.severity === "CRITICAL"
    ).length;

  const outOfStockCount =
    products.filter(
      (product) =>
        Number(
          product.quantity || 0
        ) === 0
    ).length;

  // ============================================================
  // FORMAT DATE
  // ============================================================

  function formatDate(dateValue) {
    if (!dateValue) {
      return "-";
    }

    const date =
      new Date(dateValue);

    if (
      Number.isNaN(
        date.getTime()
      )
    ) {
      return dateValue;
    }

    return date.toLocaleString();
  }

  // ============================================================
  // STATUS COLORS
  // ============================================================

  function getSeverityClass(
    severity
  ) {
    if (severity === "CRITICAL") {
      return "critical";
    }

    if (severity === "HIGH") {
      return "high";
    }

    if (severity === "MEDIUM") {
      return "medium";
    }

    return "normal";
  }

  function getStatusClass(status) {
    if (!status) {
      return "normal";
    }

    if (
      status.includes(
        "OUT OF STOCK"
      ) ||
      status.includes(
        "REORDER REQUIRED"
      )
    ) {
      return "danger";
    }

    if (
      status.includes("MONITOR") ||
      status.includes("APPROACHING")
    ) {
      return "warning";
    }

    return "success";
  }

  // ============================================================
  // NAVIGATION
  // ============================================================

  function renderNavigation() {
    const items = [
      ["dashboard", "📊", "Dashboard"],
      ["products", "📦", "Products"],
      ["movements", "🔄", "Stock Movements"],
      ["alerts", "⚠️", "Inventory Alerts"],
      ["analytics", "📈", "Demand Analytics"],
      [
        "recommendations",
        "🧠",
        "Smart Recommendations",
      ],
      ["suppliers", "🏢", "Suppliers"],
    ];

    return (
      <nav className="sidebar-nav">
        {items.map(
          ([key, icon, label]) => (
            <button
              key={key}
              className={
                activeSection === key
                  ? "nav-button active"
                  : "nav-button"
              }
              onClick={() =>
                setActiveSection(
                  key
                )
              }
            >
              <span className="nav-icon">
                {icon}
              </span>

              <span className="nav-label">
                {label}
              </span>
            </button>
          )
        )}
      </nav>
    );
  }

  // ============================================================
  // DASHBOARD
  // ============================================================

  function renderDashboard() {
    return (
      <div className="section">
        <div className="welcome-banner">
          <div>
            <span>
              WELCOME TO SMARTSTOCK
            </span>

            <h2>
              Smart Inventory Demand
              Management
            </h2>

            <p>
              Intelligent inventory
              optimization using stock
              levels, demand forecasting,
              safety stock and smart
              replenishment.
            </p>
          </div>

          <div className="welcome-icon">
            📦
          </div>
        </div>

        <div className="section-header">
          <div>
            <h2>
              Inventory Dashboard
            </h2>

            <p>
              Real-time overview of
              products, stock health,
              demand and replenishment.
            </p>
          </div>

          <button
            className="refresh-button"
            onClick={loadData}
          >
            🔄 Refresh
          </button>
        </div>

        <div className="metrics-grid">
          <div className="metric-card blue">
            <div className="metric-icon">
              📦
            </div>

            <span>
              Total Products
            </span>

            <strong>
              {totalProducts}
            </strong>

            <small>
              Products in inventory
            </small>
          </div>

          <div className="metric-card purple">
            <div className="metric-icon">
              📊
            </div>

            <span>
              Total Stock Units
            </span>

            <strong>
              {totalStock}
            </strong>

            <small>
              Available inventory
            </small>
          </div>

          <div className="metric-card orange">
            <div className="metric-icon">
              ⚠️
            </div>

            <span>
              Low Stock
            </span>

            <strong>
              {lowStockCount}
            </strong>

            <small>
              Products requiring
              attention
            </small>
          </div>

          <div className="metric-card red">
            <div className="metric-icon">
              🚨
            </div>

            <span>
              Out of Stock
            </span>

            <strong>
              {outOfStockCount}
            </strong>

            <small>
              Products unavailable
            </small>
          </div>
        </div>

        <div className="dashboard-grid">
          <div className="panel">
            <div className="panel-heading">
              <div>
                <h3>
                  Inventory Health
                </h3>

                <p>
                  Current stock condition
                  of products
                </p>
              </div>

              <span className="panel-badge">
                {products.length} Products
              </span>
            </div>

            {products.length ===
              0 ? (
              <p>
                No products available.
              </p>
            ) : (
              <div className="health-list">
                {products
                  .slice(0, 8)
                  .map((product) => {
                    const quantity =
                      Number(
                        product.quantity ||
                        0
                      );

                    const reorderLevel =
                      Number(
                        product.reorderLevel ||
                        0
                      );

                    let status =
                      "HEALTHY";

                    if (
                      quantity === 0
                    ) {
                      status =
                        "OUT OF STOCK";
                    } else if (
                      quantity <=
                      reorderLevel
                    ) {
                      status =
                        "REORDER REQUIRED";
                    } else if (
                      quantity <=
                      reorderLevel +
                      Number(
                        product.safetyStock ||
                        0
                      )
                    ) {
                      status =
                        "MONITOR";
                    }

                    return (
                      <div
                        className="health-row"
                        key={
                          product.id
                        }
                      >
                        <div className="health-product">
                          <div className="health-product-icon">
                            📦
                          </div>

                          <div>
                            <strong>
                              {
                                product.name
                              }
                            </strong>

                            <small>
                              {
                                product.sku
                              }{" "}
                              • Stock:{" "}
                              {
                                quantity
                              }
                            </small>
                          </div>
                        </div>

                        <span
                          className={getStatusClass(
                            status
                          )}
                        >
                          {status}
                        </span>
                      </div>
                    );
                  })}
              </div>
            )}
          </div>

          <div className="panel">
            <div className="panel-heading">
              <div>
                <h3>
                  Demand Forecast
                </h3>

                <p>
                  Demand prediction
                  based on sales
                  velocity
                </p>
              </div>

              <span className="panel-badge">
                Forecast
              </span>
            </div>

            {forecast ? (
              <div className="forecast-card">
                <div className="forecast-main">
                  <span>
                    Average Daily
                    Demand
                  </span>

                  <strong>
                    {Number(
                      forecast.averageDailyDemand ||
                      0
                    ).toFixed(2)}
                  </strong>

                  <small>
                    units/day
                  </small>
                </div>

                <div className="forecast-item">
                  <span>
                    7-Day Forecast
                  </span>

                  <strong>
                    {forecast.forecastNext7Days ??
                      "-"}
                  </strong>

                  <small>
                    units
                  </small>
                </div>

                <div className="forecast-item">
                  <span>
                    30-Day Forecast
                  </span>

                  <strong>
                    {forecast.forecastNext30Days ??
                      "-"}
                  </strong>

                  <small>
                    units
                  </small>
                </div>

                <div className="forecast-item">
                  <span>
                    90-Day Forecast
                  </span>

                  <strong>
                    {forecast.forecastNext90Days ??
                      "-"}
                  </strong>

                  <small>
                    units
                  </small>
                </div>
              </div>
            ) : (
              <p>
                Select a product in
                Demand Analytics to
                view demand
                forecasting.
              </p>
            )}
          </div>
        </div>

        <div className="panel">
          <div className="panel-heading">
            <div>
              <h3>
                Recent Stock Movements
              </h3>

              <p>
                Live inventory
                transaction data
              </p>
            </div>

            <span className="live-badge">
              ● Live Data
            </span>
          </div>

          {movements.length ===
            0 ? (
            <p>
              No stock movements
              found.
            </p>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>
                      Product ID
                    </th>
                    <th>
                      Movement Type
                    </th>
                    <th>
                      Quantity
                    </th>
                    <th>Date</th>
                    <th>Note</th>
                  </tr>
                </thead>

                <tbody>
                  {movements
                    .slice(0, 8)
                    .map(
                      (movement) => (
                        <tr
                          key={
                            movement.id
                          }
                        >
                          <td>
                            #
                            {
                              movement.id
                            }
                          </td>

                          <td>
                            #
                            {
                              movement.productId
                            }
                          </td>

                          <td>
                            <span
                              className={
                                movement.movementType ===
                                  "SALE"
                                  ? "movement-sale"
                                  : "movement-purchase"
                              }
                            >
                              {
                                movement.movementType
                              }
                            </span>
                          </td>

                          <td>
                            {
                              movement.quantity
                            }
                          </td>

                          <td>
                            {formatDate(
                              movement.movementDate
                            )}
                          </td>

                          <td>
                            {
                              movement.note ||
                              "-"
                            }
                          </td>
                        </tr>
                      )
                    )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    );
  }

  // ============================================================
  // PRODUCTS
  // ============================================================

  function renderProducts() {
    const isEditing =
      editingProductId !== null;

    return (
      <div className="section">
        <div className="section-header">
          <div>
            <h2>
              Product Management
            </h2>

            <p>
              Manage products and
              inventory planning
              parameters.
            </p>
          </div>
        </div>

        <div className="panel">
          <div className="section-header">
            <div>
              <h3>
                {isEditing
                  ? `Edit Product #${editingProductId}`
                  : "Add New Product"}
              </h3>

              <p>
                {isEditing
                  ? "Update product details and inventory planning parameters."
                  : "Enter product details and inventory planning parameters."}
              </p>
            </div>

            {isEditing && (
              <button
                type="button"
                className="refresh-button"
                onClick={() => {
                  resetProductForm();

                  setMessage(
                    "Edit cancelled."
                  );
                }}
              >
                ✕ Cancel Edit
              </button>
            )}
          </div>

          <form
            className="product-form"
            onSubmit={
              handleProductSubmit
            }
          >
            <input
              name="name"
              placeholder="Product Name"
              value={
                productForm.name
              }
              onChange={
                handleProductChange
              }
              required
            />

            <input
              name="sku"
              placeholder="SKU"
              value={
                productForm.sku
              }
              onChange={
                handleProductChange
              }
              required
            />

            <input
              name="description"
              placeholder="Description"
              value={
                productForm.description
              }
              onChange={
                handleProductChange
              }
            />

            <input
              name="price"
              type="number"
              placeholder="Price"
              value={
                productForm.price
              }
              onChange={
                handleProductChange
              }
              min="0"
              step="0.01"
              required
            />

            <input
              name="quantity"
              type="number"
              placeholder="Initial Quantity"
              value={
                productForm.quantity
              }
              onChange={
                handleProductChange
              }
              min="0"
              required
            />

            <input
              name="lowStockThreshold"
              type="number"
              placeholder="Low Stock Threshold"
              value={
                productForm.lowStockThreshold
              }
              onChange={
                handleProductChange
              }
              min="0"
              required
            />

            <input
              name="reorderLevel"
              type="number"
              placeholder="Reorder Level"
              value={
                productForm.reorderLevel
              }
              onChange={
                handleProductChange
              }
              min="0"
              required
            />

            <input
              name="leadTimeDays"
              type="number"
              placeholder="Lead Time Days"
              value={
                productForm.leadTimeDays
              }
              onChange={
                handleProductChange
              }
              min="0"
              required
            />

            <input
              name="safetyStock"
              type="number"
              placeholder="Safety Stock"
              value={
                productForm.safetyStock
              }
              onChange={
                handleProductChange
              }
              min="0"
              required
            />

            <input
              name="minimumOrderQuantity"
              type="number"
              placeholder="Minimum Order Quantity"
              value={
                productForm.minimumOrderQuantity
              }
              onChange={
                handleProductChange
              }
              min="0"
              required
            />

            <input
              name="maximumStockLevel"
              type="number"
              placeholder="Maximum Stock Level"
              value={
                productForm.maximumStockLevel
              }
              onChange={
                handleProductChange
              }
              min="0"
              required
            />

            <select
              name="supplierId"
              value={
                productForm.supplierId
              }
              onChange={
                handleProductChange
              }
              required
            >
              <option value="">
                Select Supplier
              </option>

              {suppliers
                .filter(
                  (supplier) =>
                    supplier.active
                )
                .map(
                  (supplier) => (
                    <option
                      key={
                        supplier.id
                      }
                      value={
                        supplier.id
                      }
                    >
                      {
                        supplier.name
                      }{" "}
                      — Lead Time:{" "}
                      {
                        supplier.leadTimeDays
                      }{" "}
                      days
                    </option>
                  )
                )}
            </select>

            {suppliers.filter(
              (supplier) =>
                supplier.active
            ).length === 0 && (
                <small
                  style={{
                    gridColumn:
                      "1 / -1",
                    color:
                      "#dc2626",
                  }}
                >
                  No active suppliers
                  available. Please add
                  an active supplier
                  first.
                </small>
              )}

            <button
              type="submit"
              className="primary-button"
            >
              {isEditing
                ? "💾 Update Product"
                : "➕ Add Product"}
            </button>

            {isEditing && (
              <button
                type="button"
                className="refresh-button"
                onClick={() => {
                  resetProductForm();

                  setMessage(
                    "Edit cancelled."
                  );
                }}
              >
                Cancel
              </button>
            )}
          </form>
        </div>

        <div className="panel">
          <h3>
            All Products
          </h3>

          {products.length ===
            0 ? (
            <p>
              No products found.
            </p>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>SKU</th>
                    <th>Price</th>
                    <th>Quantity</th>
                    <th>
                      Supplier
                    </th>
                    <th>
                      Reorder Level
                    </th>
                    <th>
                      Safety Stock
                    </th>
                    <th>
                      Lead Time
                    </th>
                    <th>MOQ</th>
                    <th>
                      Max Stock
                    </th>
                    <th>
                      Action
                    </th>
                  </tr>
                </thead>

                <tbody>
                  {products.map(
                    (product) => (
                      <tr
                        key={
                          product.id
                        }
                      >
                        <td>
                          #
                          {
                            product.id
                          }
                        </td>

                        <td>
                          {
                            product.name
                          }
                        </td>

                        <td>
                          {
                            product.sku
                          }
                        </td>

                        <td>
                          ₹
                          {
                            product.price
                          }
                        </td>

                        <td>
                          {
                            product.quantity
                          }
                        </td>

                        <td>
                          {product.supplier ? (
                            <div>
                              <strong>
                                {
                                  product
                                    .supplier
                                    .name
                                }
                              </strong>

                              <br />

                              <small>
                                Lead Time:{" "}
                                {
                                  product
                                    .supplier
                                    .leadTimeDays
                                }{" "}
                                days
                              </small>
                            </div>
                          ) : (
                            <span>
                              Not Assigned
                            </span>
                          )}
                        </td>

                        <td>
                          {
                            product.reorderLevel
                          }
                        </td>

                        <td>
                          {
                            product.safetyStock
                          }
                        </td>

                        <td>
                          {
                            product.leadTimeDays
                          }{" "}
                          days
                        </td>

                        <td>
                          {
                            product.minimumOrderQuantity
                          }
                        </td>

                        <td>
                          {
                            product.maximumStockLevel
                          }
                        </td>

                        <td>
                          <div className="action-buttons">
                            <button
                              className="edit-button"
                              onClick={() =>
                                handleEditProduct(
                                  product
                                )
                              }
                            >
                              Edit
                            </button>

                            <button
                              className="delete-button"
                              onClick={() =>
                                handleDeleteProduct(
                                  product.id
                                )
                              }
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    );
  }

  // ============================================================
  // STOCK MOVEMENTS
  // ============================================================

  function renderMovements() {
    const selectedProduct =
      getSelectedMovementProduct();

    const availableSaleStock =
      getAvailableSaleStock();

    const enteredQuantity =
      Number(
        movementForm.quantity || 0
      );

    const saleExceedsStock =
      movementForm.movementType ===
      "SALE" &&
      selectedProduct &&
      enteredQuantity >
      Number(
        selectedProduct.quantity ||
        0
      );

    return (
      <div className="section">
        <div className="section-header">
          <div>
            <h2>
              Stock Movements
            </h2>

            <p>
              Track purchases, sales,
              and inventory activity.
            </p>
          </div>

          <button
            className="refresh-button"
            onClick={loadData}
          >
            🔄 Refresh
          </button>
        </div>

        <div className="panel">
          <h3>
            Record Stock Transaction
          </h3>

          <form
            className="movement-form"
            onSubmit={
              handleCreateMovement
            }
          >
            <select
              name="productId"
              value={
                movementForm.productId
              }
              onChange={
                handleMovementChange
              }
              required
            >
              <option value="">
                Select Product
              </option>

              {products.map(
                (product) => (
                  <option
                    key={
                      product.id
                    }
                    value={
                      product.id
                    }
                  >
                    {
                      product.name
                    }{" "}
                    —{" "}
                    {
                      product.sku
                    }{" "}
                    — Stock:{" "}
                    {
                      product.quantity
                    }
                  </option>
                )
              )}
            </select>

            <select
              name="movementType"
              value={
                movementForm.movementType
              }
              onChange={
                handleMovementChange
              }
            >
              <option value="PURCHASE">
                PURCHASE
              </option>

              <option value="SALE">
                SALE
              </option>
            </select>

            <input
              name="quantity"
              type="number"
              min="1"
              max={
                movementForm.movementType ===
                  "SALE" &&
                  availableSaleStock !==
                  null
                  ? availableSaleStock
                  : undefined
              }
              placeholder="Quantity"
              value={
                movementForm.quantity
              }
              onChange={
                handleMovementChange
              }
              required
            />

            <input
              name="note"
              placeholder="Note"
              value={
                movementForm.note
              }
              onChange={
                handleMovementChange
              }
            />

            <button
              type="submit"
              className="primary-button"
              disabled={
                loading ||
                saleExceedsStock
              }
            >
              Record Movement
            </button>

            {/* ==================================================
                STOCK INFORMATION
               ================================================== */}

            {selectedProduct && (
              <small
                style={{
                  gridColumn:
                    "1 / -1",
                  color:
                    saleExceedsStock
                      ? "#dc2626"
                      : movementForm.movementType ===
                        "SALE"
                        ? "#16a34a"
                        : "#2563eb",
                  fontWeight:
                    "600",
                  lineHeight:
                    "1.6",
                }}
              >
                <strong>
                  {
                    selectedProduct.name
                  }
                </strong>{" "}
                — Current Stock:{" "}
                <strong>
                  {
                    selectedProduct.quantity
                  }{" "}
                  units
                </strong>

                {movementForm.movementType ===
                  "SALE" && (
                    <>
                      <br />

                      Maximum sale:{" "}
                      <strong>
                        {
                          selectedProduct.quantity
                        }{" "}
                        units
                      </strong>

                      {saleExceedsStock && (
                        <>
                          <br />

                          ⚠️ Sale quantity
                          exceeds available
                          stock.
                        </>
                      )}
                    </>
                  )}

                {movementForm.movementType ===
                  "PURCHASE" && (
                    <>
                      <br />

                      Purchase will increase
                      the current inventory
                      level.
                    </>
                  )}
              </small>
            )}
          </form>
        </div>

        <div className="panel">
          <h3>
            Recent Stock Movements
          </h3>

          <p className="muted">
            Live inventory
            transaction data
          </p>

          {movements.length ===
            0 ? (
            <p>
              No stock movements
              found.
            </p>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>
                      Product ID
                    </th>
                    <th>
                      Movement Type
                    </th>
                    <th>
                      Quantity
                    </th>
                    <th>Date</th>
                    <th>Note</th>
                  </tr>
                </thead>

                <tbody>
                  {movements.map(
                    (movement) => (
                      <tr
                        key={
                          movement.id
                        }
                      >
                        <td>
                          #
                          {
                            movement.id
                          }
                        </td>

                        <td>
                          #
                          {
                            movement.productId
                          }
                        </td>

                        <td>
                          <span
                            className={
                              movement.movementType ===
                                "SALE"
                                ? "movement-sale"
                                : "movement-purchase"
                            }
                          >
                            {
                              movement.movementType
                            }
                          </span>
                        </td>

                        <td>
                          {
                            movement.quantity
                          }
                        </td>

                        <td>
                          {formatDate(
                            movement.movementDate
                          )}
                        </td>

                        <td>
                          {
                            movement.note ||
                            "-"
                          }
                        </td>
                      </tr>
                    )
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    );
  }

  // ============================================================
  // INVENTORY ALERTS
  // ============================================================

  function renderAlerts() {
    return (
      <div className="section">
        <div className="section-header">
          <div>
            <h2>
              Inventory Alerts
            </h2>

            <p>
              Automated alerts based
              on current stock and
              replenishment thresholds.
            </p>
          </div>

          <button
            className="refresh-button"
            onClick={loadData}
          >
            🔄 Refresh
          </button>
        </div>

        {alerts.length ===
          0 ? (
          <div className="empty-state">
            <h3>
              ✓ No Active Inventory
              Alerts
            </h3>

            <p>
              All products are
              currently above their
              alert thresholds.
            </p>
          </div>
        ) : (
          <div className="alert-grid">
            {alerts.map(
              (alert, index) => (
                <div
                  className={`alert-card ${getSeverityClass(
                    alert.severity
                  )}`}
                  key={`${alert.productId}-${index}`}
                >
                  <div className="alert-header">
                    <div>
                      <h3>
                        {
                          alert.productName
                        }
                      </h3>

                      <span>
                        {
                          alert.sku
                        }
                      </span>
                    </div>

                    <strong>
                      {
                        alert.severity
                      }
                    </strong>
                  </div>

                  <div className="alert-status">
                    {
                      alert.status
                    }
                  </div>

                  <div className="alert-values">
                    <div>
                      <span>
                        Current Stock
                      </span>

                      <strong>
                        {
                          alert.currentStock
                        }
                      </strong>
                    </div>

                    <div>
                      <span>
                        Reorder Level
                      </span>

                      <strong>
                        {
                          alert.reorderLevel
                        }
                      </strong>
                    </div>

                    <div>
                      <span>
                        Safety Stock
                      </span>

                      <strong>
                        {
                          alert.safetyStock
                        }
                      </strong>
                    </div>

                    <div>
                      <span>
                        Effective ROP
                      </span>

                      <strong>
                        {Number(
                          alert.effectiveReorderPoint ??
                          0
                        ).toFixed(2)}
                      </strong>
                    </div>

                    <div>
                      <span>
                        Recommended
                        Order
                      </span>

                      <strong>
                        {
                          alert.recommendedOrderQuantity
                        }
                      </strong>
                    </div>
                  </div>

                  <p>
                    <strong>
                      Reason:
                    </strong>{" "}
                    {
                      alert.reason
                    }
                  </p>

                  <p>
                    <strong>
                      Action:
                    </strong>{" "}
                    {
                      alert.recommendedAction
                    }
                  </p>
                </div>
              )
            )}
          </div>
        )}
      </div>
    );
  }

  // ============================================================
  // DEMAND ANALYTICS
  // ============================================================

  function renderAnalytics() {
    return (
      <div className="section">
        <div className="section-header">
          <div>
            <h2>
              Demand Analytics
            </h2>

            <p>
              Analyze historical
              sales and forecast
              future demand.
            </p>
          </div>
        </div>

        <div className="panel">
          <label className="select-label">
            Select Product
          </label>

          <select
            className="analytics-select"
            onChange={(event) =>
              handleForecastChange(
                event.target.value
              )
            }
            defaultValue={
              products.length > 0
                ? products[0].id
                : ""
            }
          >
            <option value="">
              Select a product
            </option>

            {products.map(
              (product) => (
                <option
                  key={
                    product.id
                  }
                  value={
                    product.id
                  }
                >
                  {
                    product.name
                  }{" "}
                  —{" "}
                  {
                    product.sku
                  }
                </option>
              )
            )}
          </select>
        </div>

        {forecast ? (
          <>
            <div className="forecast-grid">
              <div className="forecast-box">
                <span>
                  Average Daily
                  Demand
                </span>

                <strong>
                  {Number(
                    forecast.averageDailyDemand ||
                    0
                  ).toFixed(2)}
                </strong>

                <small>
                  units/day
                </small>
              </div>

              <div className="forecast-box">
                <span>
                  7-Day Forecast
                </span>

                <strong>
                  {
                    forecast.forecastNext7Days ??
                    "-"
                  }
                </strong>

                <small>
                  units
                </small>
              </div>

              <div className="forecast-box">
                <span>
                  30-Day Forecast
                </span>

                <strong>
                  {
                    forecast.forecastNext30Days ??
                    "-"
                  }
                </strong>

                <small>
                  units
                </small>
              </div>

              <div className="forecast-box">
                <span>
                  90-Day Forecast
                </span>

                <strong>
                  {
                    forecast.forecastNext90Days ??
                    "-"
                  }
                </strong>

                <small>
                  units
                </small>
              </div>
            </div>

            <div className="panel">
              <h3>
                Forecast
                Interpretation
              </h3>

              <div className="formula-box">
                <strong>
                  Average Daily Demand
                </strong>

                <p>
                  Historical sales are
                  used to estimate the
                  expected daily demand
                  for the selected
                  product.
                </p>
              </div>

              <div className="formula-box">
                <strong>
                  Forecast Horizon
                </strong>

                <p>
                  The system provides
                  short-term,
                  medium-term and
                  long-term demand
                  estimates for better
                  inventory planning.
                </p>
              </div>
            </div>
          </>
        ) : (
          <div className="empty-state">
            <h3>
              No Forecast Selected
            </h3>

            <p>
              Select a product to view
              demand analytics.
            </p>
          </div>
        )}
      </div>
    );
  }

  // ============================================================
  // SMART RECOMMENDATIONS
  // ============================================================

  function renderRecommendations() {
    return (
      <div className="section">
        <div className="section-header">
          <div>
            <h2>
              Smart Replenishment
              Recommendations
            </h2>

            <p>
              Demand-aware inventory
              recommendations using
              sales velocity, lead time,
              safety stock and supplier
              constraints.
            </p>
          </div>

          <button
            className="refresh-button"
            onClick={loadData}
          >
            🔄 Refresh
          </button>
        </div>

        <div className="panel explanation-panel">
          <h3>
            How Smart Replenishment
            Works
          </h3>

          <div className="formula-grid">
            <div className="formula-box">
              <strong>
                Lead Time Demand
              </strong>

              <p>
                Average Daily Demand ×
                Lead Time Days
              </p>
            </div>

            <div className="formula-box">
              <strong>
                Demand-Based Reorder
                Point
              </strong>

              <p>
                Lead Time Demand +
                Safety Stock
              </p>
            </div>

            <div className="formula-box">
              <strong>
                Effective Reorder
                Point
              </strong>

              <p>
                Max(Reorder Level,
                Demand-Based Reorder
                Point)
              </p>
            </div>

            <div className="formula-box">
              <strong>
                Recommended Order
              </strong>

              <p>
                Target Stock Level −
                Current Stock
              </p>
            </div>
          </div>
        </div>

        {recommendations.length ===
          0 ? (
          <div className="empty-state">
            <h3>
              ✓ No Reorder Required
            </h3>

            <p>
              Current inventory levels
              are above the configured
              reorder thresholds.
            </p>
          </div>
        ) : (
          <div className="recommendation-grid">
            {recommendations.map(
              (recommendation) => (
                <RecommendationCard
                  key={
                    recommendation.productId
                  }
                  recommendation={
                    recommendation
                  }
                  onViewDetails={() =>
                    loadRecommendation(
                      recommendation.productId
                    )
                  }
                />
              )
            )}
          </div>
        )}

        {selectedRecommendation && (
          <div className="panel recommendation-detail">
            <div className="section-header">
              <div>
                <h3>
                  Recommendation Details —{" "}
                  {
                    selectedRecommendation.productName
                  }
                </h3>

                <p>
                  SKU:{" "}
                  {
                    selectedRecommendation.sku
                  }
                </p>
              </div>

              <button
                className="close-button"
                onClick={() =>
                  setSelectedRecommendation(
                    null
                  )
                }
              >
                ✕
              </button>
            </div>

            <div className="recommendation-detail-grid">
              <div>
                <span>
                  Current Stock
                </span>

                <strong>
                  {
                    selectedRecommendation.currentStock
                  }
                </strong>
              </div>

              <div>
                <span>
                  Reorder Level
                </span>

                <strong>
                  {
                    selectedRecommendation.reorderLevel
                  }
                </strong>
              </div>

              <div>
                <span>
                  Average Daily Demand
                </span>

                <strong>
                  {Number(
                    selectedRecommendation.averageDailyDemand ??
                    0
                  ).toFixed(2)}
                </strong>
              </div>

              <div>
                <span>
                  Lead Time
                </span>

                <strong>
                  {
                    selectedRecommendation.leadTimeDays
                  }{" "}
                  days
                </strong>
              </div>

              <div>
                <span>
                  Lead Time Demand
                </span>

                <strong>
                  {Number(
                    selectedRecommendation.leadTimeDemand ??
                    0
                  ).toFixed(2)}
                </strong>
              </div>

              <div>
                <span>
                  Safety Stock
                </span>

                <strong>
                  {
                    selectedRecommendation.safetyStock
                  }
                </strong>
              </div>

              <div>
                <span>
                  Demand-Based ROP
                </span>

                <strong>
                  {Number(
                    selectedRecommendation.demandBasedReorderPoint ??
                    0
                  ).toFixed(2)}
                </strong>
              </div>

              <div>
                <span>
                  Effective Reorder Point
                </span>

                <strong>
                  {Number(
                    selectedRecommendation.effectiveReorderPoint ??
                    0
                  ).toFixed(2)}
                </strong>
              </div>

              <div>
                <span>
                  Target Stock
                </span>

                <strong>
                  {
                    selectedRecommendation.targetStock
                  }
                </strong>
              </div>

              <div>
                <span>
                  Minimum Order Quantity
                </span>

                <strong>
                  {
                    selectedRecommendation.minimumOrderQuantity
                  }
                </strong>
              </div>

              <div>
                <span>
                  Maximum Stock Level
                </span>

                <strong>
                  {
                    selectedRecommendation.maximumStockLevel
                  }
                </strong>
              </div>

              <div className="highlight-value">
                <span>
                  Recommended Order
                </span>

                <strong>
                  {
                    selectedRecommendation.recommendedOrderQuantity
                  }
                </strong>
              </div>

              <div>
                <span>
                  Status
                </span>

                <strong
                  className={getStatusClass(
                    selectedRecommendation.status
                  )}
                >
                  {
                    selectedRecommendation.status
                  }
                </strong>
              </div>
            </div>

            <div className="calculation-box">
              <h4>
                Smart Calculation
              </h4>

              <p>
                Lead Time Demand ={" "}
                {Number(
                  selectedRecommendation.averageDailyDemand ??
                  0
                ).toFixed(2)}{" "}
                ×{" "}
                {
                  selectedRecommendation.leadTimeDays
                }{" "}
                ={" "}
                {Number(
                  selectedRecommendation.leadTimeDemand ??
                  0
                ).toFixed(2)}
              </p>

              <p>
                Demand-Based Reorder
                Point = Lead Time Demand
                + Safety Stock
              </p>

              <p>
                Demand-Based Reorder
                Point ={" "}
                {Number(
                  selectedRecommendation.leadTimeDemand ??
                  0
                ).toFixed(2)}{" "}
                +{" "}
                {
                  selectedRecommendation.safetyStock
                }{" "}
                ={" "}
                {Number(
                  selectedRecommendation.demandBasedReorderPoint ??
                  0
                ).toFixed(2)}
              </p>

              <p>
                Effective Reorder Point =
                max(Reorder Level,
                Demand-Based Reorder
                Point)
              </p>

              <p>
                Effective Reorder Point =
                max(
                {
                  selectedRecommendation.reorderLevel
                }
                ,{" "}
                {Number(
                  selectedRecommendation.demandBasedReorderPoint ??
                  0
                ).toFixed(2)}
                ) ={" "}
                {Number(
                  selectedRecommendation.effectiveReorderPoint ??
                  0
                ).toFixed(2)}
              </p>

              <p>
                Target Stock ={" "}
                {
                  selectedRecommendation.targetStock
                }{" "}
                units
              </p>

              <p>
                Initial Recommended
                Order = Target Stock −
                Current Stock
              </p>

              <p>
                Initial Recommended
                Order ={" "}
                {
                  selectedRecommendation.targetStock
                }{" "}
                −{" "}
                {
                  selectedRecommendation.currentStock
                }{" "}
                ={" "}
                {Math.max(
                  0,
                  Number(
                    selectedRecommendation.targetStock ??
                    0
                  ) -
                  Number(
                    selectedRecommendation.currentStock ??
                    0
                  )
                )}{" "}
                units
              </p>

              <p>
                Minimum Order Quantity ={" "}
                {
                  selectedRecommendation.minimumOrderQuantity
                }{" "}
                units
              </p>

              <p>
                Final Recommended Order ={" "}
                {
                  selectedRecommendation.recommendedOrderQuantity
                }{" "}
                units
              </p>
            </div>
          </div>
        )}
      </div>
    );
  }

  // ============================================================
  // SUPPLIERS
  // ============================================================

  function renderSuppliers() {
    const isEditing =
      editingSupplierId !== null;

    return (
      <div className="section">
        <div className="section-header">
          <div>
            <h2>
              Supplier Management
            </h2>

            <p>
              Manage suppliers and
              supplier lead-time
              information for inventory
              replenishment.
            </p>
          </div>

          <button
            className="refresh-button"
            onClick={loadData}
          >
            🔄 Refresh
          </button>
        </div>

        <div className="panel">
          <div className="section-header">
            <div>
              <h3>
                {isEditing
                  ? `Edit Supplier #${editingSupplierId}`
                  : "Add New Supplier"}
              </h3>

              <p>
                {isEditing
                  ? "Update supplier information."
                  : "Enter supplier information."}
              </p>
            </div>

            {isEditing && (
              <button
                type="button"
                className="refresh-button"
                onClick={() => {
                  resetSupplierForm();

                  setMessage(
                    "Edit cancelled."
                  );
                }}
              >
                ✕ Cancel Edit
              </button>
            )}
          </div>

          <form
            className="product-form"
            onSubmit={
              handleSupplierSubmit
            }
          >
            <input
              name="name"
              placeholder="Supplier Name"
              value={
                supplierForm.name
              }
              onChange={
                handleSupplierChange
              }
              required
            />

            <input
              name="contactPerson"
              placeholder="Contact Person"
              value={
                supplierForm.contactPerson
              }
              onChange={
                handleSupplierChange
              }
              required
            />

            <input
              name="email"
              type="email"
              placeholder="Email"
              value={
                supplierForm.email
              }
              onChange={
                handleSupplierChange
              }
              required
            />

            <input
              name="phone"
              placeholder="Phone"
              value={
                supplierForm.phone
              }
              onChange={
                handleSupplierChange
              }
              required
            />

            <input
              name="address"
              placeholder="Address"
              value={
                supplierForm.address
              }
              onChange={
                handleSupplierChange
              }
              required
            />

            <input
              name="leadTimeDays"
              type="number"
              placeholder="Lead Time Days"
              value={
                supplierForm.leadTimeDays
              }
              onChange={
                handleSupplierChange
              }
              min="0"
              required
            />

            <label className="checkbox-label">
              <input
                name="active"
                type="checkbox"
                checked={
                  supplierForm.active
                }
                onChange={
                  handleSupplierChange
                }
              />

              <span>
                Active Supplier
              </span>
            </label>

            <button
              type="submit"
              className="primary-button"
            >
              {isEditing
                ? "💾 Update Supplier"
                : "➕ Add Supplier"}
            </button>

            {isEditing && (
              <button
                type="button"
                className="refresh-button"
                onClick={() => {
                  resetSupplierForm();

                  setMessage(
                    "Edit cancelled."
                  );
                }}
              >
                Cancel
              </button>
            )}
          </form>
        </div>

        <div className="panel">
          <div className="panel-heading">
            <div>
              <h3>
                All Suppliers
              </h3>

              <p>
                Supplier master data
                available in SmartStock.
              </p>
            </div>

            <span className="panel-badge">
              {suppliers.length} Suppliers
            </span>
          </div>

          {suppliers.length ===
            0 ? (
            <div className="empty-state">
              <h3>
                No Suppliers Found
              </h3>

              <p>
                Add a supplier to begin
                supplier management.
              </p>
            </div>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Supplier</th>
                    <th>
                      Contact Person
                    </th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Address</th>
                    <th>
                      Lead Time
                    </th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>

                <tbody>
                  {suppliers.map(
                    (supplier) => (
                      <tr
                        key={
                          supplier.id
                        }
                      >
                        <td>
                          #
                          {
                            supplier.id
                          }
                        </td>

                        <td>
                          <strong>
                            {
                              supplier.name
                            }
                          </strong>
                        </td>

                        <td>
                          {
                            supplier.contactPerson ||
                            "-"
                          }
                        </td>

                        <td>
                          {
                            supplier.email ||
                            "-"
                          }
                        </td>

                        <td>
                          {
                            supplier.phone ||
                            "-"
                          }
                        </td>

                        <td>
                          {
                            supplier.address ||
                            "-"
                          }
                        </td>

                        <td>
                          {
                            supplier.leadTimeDays ??
                            0
                          }{" "}
                          days
                        </td>

                        <td>
                          <span
                            className={
                              supplier.active
                                ? "success"
                                : "danger"
                            }
                          >
                            {supplier.active
                              ? "ACTIVE"
                              : "INACTIVE"}
                          </span>
                        </td>

                        <td>
                          <div className="action-buttons">
                            <button
                              className="edit-button"
                              onClick={() =>
                                handleEditSupplier(
                                  supplier
                                )
                              }
                            >
                              Edit
                            </button>

                            <button
                              className="delete-button"
                              onClick={() =>
                                handleDeleteSupplier(
                                  supplier.id
                                )
                              }
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    );
  }

  // ============================================================
  // APP CONTENT
  // ============================================================

  function renderContent() {
    switch (activeSection) {
      case "products":
        return renderProducts();

      case "movements":
        return renderMovements();

      case "alerts":
        return renderAlerts();

      case "analytics":
        return renderAnalytics();

      case "recommendations":
        return renderRecommendations();

      case "suppliers":
        return renderSuppliers();

      case "dashboard":
      default:
        return renderDashboard();
    }
  }

  // ============================================================
  // MAIN APP
  // ============================================================

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">
            📦
          </div>

          <div>
            <h2>
              SmartStock
            </h2>

            <span>
              Inventory Intelligence
            </span>
          </div>
        </div>

        {renderNavigation()}

        <div className="sidebar-footer">
          <span>
            Smart Inventory
          </span>

          <small>
            Demand Management
            System
          </small>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <p className="welcome">
              SMART INVENTORY
              PLATFORM
            </p>

            <h1>
              SmartStock Inventory
              System
            </h1>

            <p>
              Intelligent inventory
              optimization and demand
              management
            </p>
          </div>

          <div className="system-status">
            <span className="status-dot"></span>

            Backend Connected

            <div className="avatar">
              SS
            </div>
          </div>
        </header>

        {message && (
          <div className="message-bar">
            <span>
              {message}
            </span>

            <button
              onClick={() =>
                setMessage("")
              }
            >
              ✕
            </button>
          </div>
        )}

        {loading && (
          <div className="loading-bar">
            Processing...
          </div>
        )}

        <div className="content">
          {renderContent()}
        </div>
      </main>
    </div>
  );
}

// ============================================================
// RECOMMENDATION CARD
// ============================================================

function RecommendationCard({
  recommendation,
  onViewDetails,
}) {
  const currentStock =
    Number(
      recommendation.currentStock ||
      0
    );

  const maximumStock =
    Number(
      recommendation.maximumStockLevel ||
      0
    );

  const stockPercentage =
    maximumStock > 0
      ? Math.min(
        100,
        Math.max(
          0,
          (currentStock /
            maximumStock) *
          100
        )
      )
      : 0;

  return (
    <div className="recommendation-card">
      <div className="recommendation-header">
        <div>
          <h3>
            {
              recommendation.productName
            }
          </h3>

          <span>
            {
              recommendation.sku
            }
          </span>
        </div>

        <span className="recommendation-status">
          {
            recommendation.status
          }
        </span>
      </div>

      <div className="stock-bar-container">
        <div className="stock-bar-label">
          <span>
            Current Stock
          </span>

          <strong>
            {currentStock} /{" "}
            {maximumStock > 0
              ? maximumStock
              : "∞"}
          </strong>
        </div>

        <div className="stock-bar">
          <div
            className="stock-bar-fill"
            style={{
              width: `${stockPercentage}%`,
            }}
          ></div>
        </div>
      </div>

      <div className="recommendation-values">
        <div>
          <span>
            Daily Demand
          </span>

          <strong>
            {Number(
              recommendation.averageDailyDemand ||
              0
            ).toFixed(2)}
          </strong>
        </div>

        <div>
          <span>
            Lead Time
          </span>

          <strong>
            {
              recommendation.leadTimeDays
            }{" "}
            days
          </strong>
        </div>

        <div>
          <span>
            Lead Time Demand
          </span>

          <strong>
            {Number(
              recommendation.leadTimeDemand ||
              0
            ).toFixed(2)}
          </strong>
        </div>

        <div>
          <span>
            Safety Stock
          </span>

          <strong>
            {
              recommendation.safetyStock
            }
          </strong>
        </div>

        <div>
          <span>
            Effective ROP
          </span>

          <strong>
            {Number(
              recommendation.effectiveReorderPoint ??
              recommendation.dynamicReorderPoint ??
              0
            ).toFixed(2)}
          </strong>
        </div>

        <div>
          <span>
            MOQ
          </span>

          <strong>
            {
              recommendation.minimumOrderQuantity
            }
          </strong>
        </div>
      </div>

      <div className="recommended-order">
        <span>
          Recommended Order Quantity
        </span>

        <strong>
          {
            recommendation.recommendedOrderQuantity
          }
        </strong>

        <small>
          units
        </small>
      </div>

      <button
        className="primary-button full-width"
        onClick={
          onViewDetails
        }
      >
        View Smart Calculation
      </button>
    </div>
  );
}

export default App;