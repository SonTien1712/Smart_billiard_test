import React, { useEffect, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Chart, ChartContainer, ChartTooltip, ChartTooltipContent } from '../ui/chart';
import { PageType } from '../Dashboard';
import { Building, Table, Users, Calendar, Package, DollarSign, TrendingUp, Activity } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, ResponsiveContainer, LineChart, Line } from 'recharts';

// *** THÊM DÒNG NÀY: Import service và Skeleton (để hiển thị loading) ***
import { customerService } from '../../services/customerService';
import { Skeleton } from '../ui/skeleton';
// (Lưu ý: bạn có thể phải sửa đường dẫn import 'customerService' nếu nó nằm ở chỗ khác)

// Xóa import 'useAuth' vì nó không được dùng trong file này
// import { useAuth } from '../AuthProvider';


export function CustomerDashboard({ onPageChange }) {

    // *** THÊM DÒNG NÀY: Khai báo state cho stats và loading ***
    const [stats, setStats] = useState(null); // Khởi tạo là null
    const [revenueData, setRevenueData] = useState([]); // Khởi tạo là mảng rỗng
    const [tableUsageData, setTableUsageData] = useState([]); // Khởi tạo là mảng rỗng
    const [isLoading, setIsLoading] = useState(true); // Thêm state loading

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const data = await customerService.getDashboardStats();
                setStats(data);

                // Ghi chú: API backend của bạn có thể chưa trả về revenueData / tableUsageData
                // setRevenueData(data.revenueData || []);
                // setTableUsageData(data.tableUsageData || []);

            } catch (error) {
                console.error('Failed to load dashboard:', error);
            } finally {
                // *** THÊM DÒNG NÀY: Dừng loading sau khi API chạy xong (kể cả khi lỗi) ***
                setIsLoading(false);
            }
        };
        fetchStats();
    }, []);

    // *** THÊM KHỐI NÀY: Kiểm tra loading trước khi render ***
    if (isLoading) {
        return (
            <div className="space-y-6">
                <h1 className="text-3xl font-semibold">Club Dashboard</h1>
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
                    {/* Hiển thị Skeleton thay vì crash */}
                    <Skeleton className="h-[120px] w-full" />
                    <Skeleton className="h-[120px] w-full" />
                    <Skeleton className="h-[120px] w-full" />
                    <Skeleton className="h-[120px] w-full" />
                    <Skeleton className="h-[120px] w-full" />
                    <Skeleton className="h-[120px] w-full" />
                </div>
            </div>
        );
    }

    // *** THÊM KHỐI NÀY: Kiểm tra nếu API lỗi và stats vẫn là null ***
    if (!stats) {
        return (
            <div className="space-y-6">
                <h1 className="text-3xl font-semibold">Club Dashboard</h1>
                <Card>
                    <CardHeader>
                        <CardTitle className="text-destructive">Lỗi</CardTitle>
                        <CardDescription>
                            Không thể tải dữ liệu dashboard. Vui lòng thử lại sau.
                        </CardDescription>
                    </CardHeader>
                </Card>
            </div>
        );
    }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-semibold">Club Dashboard</h1>
        <p className="text-muted-foreground">Overview of your billiards club operations</p>
      </div>

      {/* KPI Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Revenue</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${stats.totalRevenue.toLocaleString()}</div>
            <p className="text-xs text-muted-foreground">
              +{stats.monthlyGrowth}% from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Tables</CardTitle>
            <Table className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.totalTables}</div>
            <p className="text-xs text-muted-foreground">
              Billiard tables
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Employees</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.totalEmployees}</div>
            <p className="text-xs text-muted-foreground">
              Total staff members
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Shifts</CardTitle>
            <Calendar className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.activeShifts}</div>
            <p className="text-xs text-muted-foreground">
              Currently working
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Products</CardTitle>
            <Package className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.totalProducts}</div>
            <p className="text-xs text-muted-foreground">
              Available items
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Growth</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">+{stats.monthlyGrowth}%</div>
            <p className="text-xs text-muted-foreground">
              Monthly growth
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Charts */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Revenue Trend</CardTitle>
            <CardDescription>Daily revenue for the past week</CardDescription>
          </CardHeader>
          <CardContent>
            <ChartContainer
              config={{
                revenue: {
                  label: "Revenue",
                  color: "var(--chart-1)",
                },
              }}
              className="h-[200px]"
            >
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={revenueData}>
                  <XAxis dataKey="date" tickFormatter={(value) => new Date(value).toLocaleDateString()} />
                  <YAxis />
                  <ChartTooltip content={<ChartTooltipContent />} />
                  <Line type="monotone" dataKey="revenue" stroke="var(--chart-1)" strokeWidth={2} />
                </LineChart>
              </ResponsiveContainer>
            </ChartContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Table Usage</CardTitle>
            <CardDescription>Hours played per table today</CardDescription>
          </CardHeader>
          <CardContent>
            <ChartContainer
              config={{
                hours: {
                  label: "Hours",
                  color: "var(--chart-2)",
                },
              }}
              className="h-[200px]"
            >
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={tableUsageData}>
                  <XAxis dataKey="table" />
                  <YAxis />
                  <ChartTooltip content={<ChartTooltipContent />} />
                  <Bar dataKey="hours" fill="var(--chart-2)" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </ChartContainer>
          </CardContent>
        </Card>
      </div>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>Manage your club efficiently</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Button 
              variant="outline" 
              className="h-20 flex flex-col space-y-2"
              onClick={() => onPageChange('tables')}
            >
              <Table className="h-6 w-6" />
              <span>Manage Tables</span>
            </Button>
            <Button 
              variant="outline" 
              className="h-20 flex flex-col space-y-2"
              onClick={() => onPageChange('staff')}
            >
              <Users className="h-6 w-6" />
              <span>Manage Staff</span>
            </Button>
            <Button 
              variant="outline" 
              className="h-20 flex flex-col space-y-2"
              onClick={() => onPageChange('shifts')}
            >
              <Calendar className="h-6 w-6" />
              <span>Work Shifts</span>
            </Button>
            <Button 
              variant="outline" 
              className="h-20 flex flex-col space-y-2"
              onClick={() => onPageChange('products')}
            >
              <Package className="h-6 w-6" />
              <span>Manage Products</span>
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}