import { apiClient } from './api';
import { API_CONFIG } from '../config/api';
import api from './api';

const USE_MOCK_DATA = false;

export class CustomerService {

    // ==================== DASHBOARD ====================

    async getDashboardStats() {
        try {
            console.log('[API Dashboard] Fetching dashboard stats...');

            // ✅ ĐÃ SỬA: Trỏ đến đúng endpoint của CustomerController
            // Endpoint cũ: '/customers/dashboard-stats'
            // Endpoint mới dựa trên CustomerController.java: '/api/v1/customer/dashboard-stats'
            // Giả sử apiClient đã xử lý prefix '/api/v1'
            // Nếu không, bạn cần dùng đường dẫn đầy đủ.
            // Dựa trên các hàm khác, có vẻ bạn đang dùng prefix.
            // Chúng ta sẽ dùng endpoint tương đối với prefix của CustomerController.

            // LƯU Ý: Nếu 'apiClient' của bạn tự động thêm '/api/v1',
            // thì endpoint chỉ cần là '/customer/dashboard-stats'.
            // Nếu không, nó phải là '/api/v1/customer/dashboard-stats'.

            // *** Giả định an toàn nhất là dùng đường dẫn mà Controller của bạn định nghĩa ***
            // Bạn đã import 'apiClient', chúng ta sẽ dùng nó.

            // Hãy kiểm tra 'api.js' của bạn.
            // Nếu 'api.js' đặt baseURL là 'http://localhost:8080/api/v1',
            // thì endpoint ở đây phải là '/customer/dashboard-stats'.

            // Dựa trên cách bạn gọi:
            // const response = await api.get('/customer/dashboard-stats');
            // (từ đoạn code lỗi của bạn ở dưới)
            // và CustomerController.java của bạn là:
            // @RequestMapping("/api/v1/customer")
            // @GetMapping("/dashboard-stats")

            // => Endpoint chính xác để gọi là: '/customer/dashboard-stats'
            // (Nếu apiClient được cấu hình với baseURL là /api/v1)
            // Hoặc là '/api/v1/customer/dashboard-stats' (Nếu apiClient gọi từ root)

            // Tôi sẽ sử dụng endpoint mà tôi đã đề xuất ở backend,
            // vì nó tuân thủ RESTful với prefix của controller.

            // Giả sử 'apiClient' của bạn KHÔNG có prefix /api/v1
            const endpoint = '/api/v1/customer/dashboard-stats';

            // *** NẾU 'apiClient' CÓ prefix /api/v1, hãy dùng dòng này: ***
            // const endpoint = '/customer/dashboard-stats';

            const data = await apiClient.get(endpoint);

            console.log('[API Dashboard] Stats received:', data);
            return data;
        } catch (error) {
            console.error('[API Dashboard] Failed to fetch stats:', error);
            // Ném lỗi đã được xử lý (nếu có) hoặc thông báo chung
            throw error.response?.data || error.message || 'Failed to fetch dashboard stats';
        }
    }

    /**
     * Get dashboard statistics for specific customer (admin use)
     */
    async getDashboardStatsByCustomerId(customerId) {
        try {
            console.log('[API Dashboard] Fetching stats for customer:', customerId);

            // ✅ LƯU Ý: Endpoint này có thể cũng cần sửa
            // Hiện tại: '/customers/${customerId}/dashboard-stats'
            // Có thể cần: '/api/v1/admin/customer/${customerId}/dashboard-stats' (tùy vào AdminController)
            const endpoint = `/customers/${customerId}/dashboard-stats`;
            const data = await apiClient.get(endpoint);

            return data;
        } catch (error) {
            console.error('[API Dashboard] Failed to fetch stats:', error);
            throw error;
        }
    }

    // ==================== CLUB MANAGEMENT ====================

    async getClubsByCustomer(customerId) {
        if (USE_MOCK_DATA) {
            return MockService.getClubsByCustomer(customerId);
        }

        try {
            const endpoint = API_CONFIG.ENDPOINTS.CUSTOMER.CLUBS_BY_CUSTOMER(customerId);
            console.log('[API Club] Fetching from:', endpoint);

            const data = await apiClient.get(endpoint);

            console.log('[API Club] Raw data:', data);
            let clubData;

            if (Array.isArray(data)) {
                clubData = data;
            } else if (data && Array.isArray(data.data)) {
                clubData = data.data;
            } else if (data && Array.isArray(data.clubs)) {
                clubData = data.clubs;
            } else {
                console.warn('[API Club] Unexpected response:', data);
                clubData = [];
            }

            console.log('[API Club] Final club data:', clubData);
            return clubData;
        } catch (error) {
            const status = error.response?.status;
            const errData = error.response?.data || error.message;

            console.error('[API Club] FAILED:', error);
            console.error('[API Club] Status:', status || 'Network/CORS');
            console.error('[API Club] Error data:', errData);

            return [];
        }
    }

    async createClub(clubData) {
        if (USE_MOCK_DATA) {
            return MockService.createClub(clubData);
        }

        const response = await apiClient.post(
            API_CONFIG.ENDPOINTS.CUSTOMER.CLUBS,
            clubData
        );
        return response.data;
    }

    async updateClub(id, clubData) {
        if (USE_MOCK_DATA) {
            return MockService.updateClub(id, clubData);
        }

        const response = await apiClient.put(
            `${API_CONFIG.ENDPOINTS.CUSTOMER.CLUBS}/${id}`,
            clubData
        );
        return response.data;
    }

    async deleteClub(id) {
        if (USE_MOCK_DATA) {
            return MockService.deleteClub(id);
        }

        await apiClient.delete(`${API_CONFIG.ENDPOINTS.CUSTOMER.CLUBS}/${id}`);
    }

    // ==================== TABLE MANAGEMENT ====================

    async getTables(clubId, params) {
        const query = { clubId, ...(params || {}) };
        const response = await apiClient.get(API_CONFIG.ENDPOINTS.CUSTOMER.TABLES, query);
        return response.data;
    }

    async getTablesByCustomerId(customerId) {
        try {
            const endpoint = `${API_CONFIG.ENDPOINTS.CUSTOMER.TABLES_BY_CUSTOMER(customerId)}`;
            console.log('[API Table] Fetching from:', endpoint);

            const data = await apiClient.get(endpoint);

            console.log('[API Table] Raw data:', data);
            let tableData;

            if (Array.isArray(data)) {
                tableData = data;
            } else if (data && Array.isArray(data.data)) {
                tableData = data.data;
            } else {
                console.warn('[API Table] Unexpected response:', data);
                tableData = [];
            }

            console.log('[API Table] Final table data:', tableData);
            return tableData;
        } catch (error) {
            const status = error.response?.status;
            const errData = error.response?.data || error.message;

            console.error('[API Table] FAILED:', error);
            console.error('[API Table] Status:', status || 'Network/CORS');
            console.error('[API Table] Error data:', errData);

            return [];
        }
    }

    async createTable(tableData) {
        const response = await apiClient.post(API_CONFIG.ENDPOINTS.CUSTOMER.TABLES, tableData);
        return response.data;
    }

    async updateTable(id, tableData) {
        const response = await apiClient.put(
            `${API_CONFIG.ENDPOINTS.CUSTOMER.TABLES}/${id}`,
            tableData
        );
        return response.data;
    }

    async deleteTable(id) {
        await apiClient.delete(`${API_CONFIG.ENDPOINTS.CUSTOMER.TABLES}/${id}`);
    }

    // ==================== STAFF MANAGEMENT ====================

    async getStaff(clubId, params) {
        const query = { clubId, ...(params || {}) };
        const response = await apiClient.get(API_CONFIG.ENDPOINTS.CUSTOMER.STAFF, query);
        return response.data;
    }

    async createStaff(staffData) {
        const response = await apiClient.post(API_CONFIG.ENDPOINTS.CUSTOMER.STAFF, staffData);
        return response.data;
    }

    async updateStaff(id, staffData) {
        const response = await apiClient.put(
            `${API_CONFIG.ENDPOINTS.CUSTOMER.STAFF}/${id}`,
            staffData
        );
        return response.data;
    }

    async deleteStaff(id) {
        await apiClient.delete(`${API_CONFIG.ENDPOINTS.CUSTOMER.STAFF}/${id}`);
    }

    // ==================== STAFF ACCOUNT MANAGEMENT ====================

    async getStaffAccounts(clubId, params) {
        const query = { clubId, ...(params || {}) };
        const response = await apiClient.get(API_CONFIG.ENDPOINTS.CUSTOMER.STAFF_ACCOUNTS, query);
        return response.data;
    }

    async createStaffAccount(accountData) {
        const response = await apiClient.post(API_CONFIG.ENDPOINTS.CUSTOMER.STAFF_ACCOUNTS, accountData);
        return response.data;
    }

    async updateStaffAccount(id, accountData) {
        const response = await apiClient.put(
            `${API_CONFIG.ENDPOINTS.CUSTOMER.STAFF_ACCOUNTS}/${id}`,
            accountData
        );
        return response.data;
    }

    async deleteStaffAccount(id) {
        await apiClient.delete(`${API_CONFIG.ENDPOINTS.CUSTOMER.STAFF_ACCOUNTS}/${id}`);
    }

    // ==================== SHIFT MANAGEMENT ====================

    async getShifts(clubId, params) {
        const query = { clubId, ...(params || {}) };
        const response = await apiClient.get(API_CONFIG.ENDPOINTS.CUSTOMER.SHIFTS, query);
        return response.data;
    }

    async createShift(shiftData) {
        const response = await apiClient.post(API_CONFIG.ENDPOINTS.CUSTOMER.SHIFTS, shiftData);
        return response.data;
    }

    async updateShift(id, shiftData) {
        const response = await apiClient.put(
            `${API_CONFIG.ENDPOINTS.CUSTOMER.SHIFTS}/${id}`,
            shiftData
        );
        return response.data;
    }

    async deleteShift(id) {
        await apiClient.delete(`${API_CONFIG.ENDPOINTS.CUSTOMER.SHIFTS}/${id}`);
    }

    // ==================== PROMOTION MANAGEMENT ====================

    async getPromotions(clubId, params) {
        const query = { clubId, ...(params || {}) };
        const response = await apiClient.get(API_CONFIG.ENDPOINTS.CUSTOMER.PROMOTIONS, query);
        return response.promotions ?? response.data ?? response;
    }

    async createPromotion(promotionData) {
        const response = await apiClient.post(API_CONFIG.ENDPOINTS.CUSTOMER.PROMOTIONS, promotionData);
        return response;
    }

    async updatePromotion(id, promotionData) {
        const response = await apiClient.put(
            `${API_CONFIG.ENDPOINTS.CUSTOMER.PROMOTIONS}/${id}`,
            promotionData
        );
        return response;
    }

    async deletePromotion(id) {
        await apiClient.delete(`${API_CONFIG.ENDPOINTS.CUSTOMER.PROMOTIONS}/${id}`);
    }

    // ==================== PRODUCT MANAGEMENT ====================

    async getProducts(clubId, params) {
        try {
            const query = { clubId, ...(params || {}) };
            console.log('[API Product] Fetching products with query:', query);

            const response = await apiClient.get(
                API_CONFIG.ENDPOINTS.CUSTOMER.PRODUCTS,
                query
            );

            console.log('[API Product] Raw response:', response);

            let productData;
            if (Array.isArray(response)) {
                productData = response;
            } else if (response && Array.isArray(response.data)) {
                productData = response.data;
            } else if (response && Array.isArray(response.products)) {
                productData = response.products;
            } else {
                console.warn('[API Product] Unexpected response:', response);
                productData = [];
            }

            console.log('[API Product] Final product data:', productData);
            return productData;
        } catch (error) {
            console.error('[API Product] Failed to fetch:', error);
            return [];
        }
    }

    async getProductById(id, clubId) {
        try {
            const endpoint = `${API_CONFIG.ENDPOINTS.CUSTOMER.PRODUCTS}/${id}`;
            console.log('[API Product] Fetching product by ID:', id);

            const response = await apiClient.get(endpoint, { clubId });
            return response.data || response;
        } catch (error) {
            console.error('[API Product] Failed to fetch by ID:', error);
            throw error;
        }
    }

    async createProduct(productData) {
        try {
            console.log('[API Product] Creating product:', productData);

            const response = await apiClient.post(
                API_CONFIG.ENDPOINTS.CUSTOMER.PRODUCTS,
                productData
            );

            return response.data || response;
        } catch (error) {
            console.error('[API Product] Failed to create:', error);
            throw error;
        }
    }

    async updateProduct(id, productData) {
        try {
            console.log('[API Product] Updating product:', id, productData);

            const response = await apiClient.put(
                `${API_CONFIG.ENDPOINTS.CUSTOMER.PRODUCTS}/${id}`,
                productData
            );

            return response.data || response;
        } catch (error) {
            console.error('[API Product] Failed to update:', error);
            throw error;
        }
    }

    async deleteProduct(id) {
        try {
            console.log('[API Product] Deleting product:', id);

            await apiClient.delete(
                `${API_CONFIG.ENDPOINTS.CUSTOMER.PRODUCTS}/${id}`
            );
        } catch (error) {
            console.error('[API Product] Failed to delete:', error);
            throw error;
        }
    }

    async toggleProductStatus(id) {
        try {
            console.log('[API Product] Toggling product status:', id);

            const response = await apiClient.patch(
                `${API_CONFIG.ENDPOINTS.CUSTOMER.PRODUCTS}/${id}/toggle-status`
            );

            return response.data || response;
        } catch (error) {
            console.error('[API Product] Failed to toggle status:', error);
            throw error;
        }
    }

    async searchProducts(clubId, keyword) {
        try {
            console.log('[API Product] Searching products:', { clubId, keyword });

            const response = await apiClient.get(
                `${API_CONFIG.ENDPOINTS.CUSTOMER.PRODUCTS}/search`,
                { clubId, keyword }
            );

            let productData;
            if (Array.isArray(response)) {
                productData = response;
            } else if (response && Array.isArray(response.data)) {
                productData = response.data;
            } else {
                productData = [];
            }

            return productData;
        } catch (error) {
            console.error('[API Product] Failed to search:', error);
            return [];
        }
    }




}

export const customerService = new CustomerService();

