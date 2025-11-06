import React, { useEffect, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Chart, ChartContainer, ChartTooltip, ChartTooltipContent } from '../ui/chart';
import { Building, Table, Users, Calendar, Package, DollarSign, TrendingUp, Activity, AlertCircle } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, ResponsiveContainer, LineChart, Line } from 'recharts';
import { customerService } from '../../services/customerService';
import { useAuth } from '../AuthProvider';

export function CustomerDashboard({ onPageChange }) {
    const { user } = useAuth();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchDashboardStats();
    }, []);

    const fetchDashboardStats = async () => {
        try {
            setLoading(true);
            setError(null);

            // Call API to get dashboard statistics
            const response = await customerService.getDashboardStats();

            console.log('[Dashboard] Stats received:', response);
            setStats(response);
        } catch (err) {
            console.error('[Dashboard] Failed to fetch stats:', err);
            setError('Failed to load dashboard statistics');
        } finally {
            setLoading(false);
        }
    };

    // Format currency
    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount || 0);
    };

    // Format date for chart
    const formatDate = (dateStr) => {
        const date = new Date(dateStr);
        return `${date.getDate()}/${date.getMonth() + 1}`;
    };

    // Loading state
    if (loading) {
        return (
            <div className="flex items-center justify-center h-96">
                <div className="flex flex-col items-center space-y-4">
                    <Activity className="h-12 w-12 animate-spin text-primary" />
                    <p className="text-muted-foreground">Loading dashboard...</p>
                </div>
            </div>
        );
    }

    // Error state
    if (error) {
        return (
            <div className="flex items-center justify-center h-96">
                <Card className="w-96">
                    <CardContent className="pt-6">
                        <div className="flex flex-col items-center space-y-4">
                            <AlertCircle className="h-12 w-12 text-destructive" />
                            <p className="text-center text-muted-foreground">{error}</p>
                            <Button onClick={fetchDashboardStats}>Retry</Button>
                        </div>
                    </CardContent>
                </Card>
            </div>
        );
    }

    // Prepare chart data
    const revenueData = stats?.revenueData?.map(item => ({
        date: formatDate(item.date),
        revenue: item.revenue || 0
    })) || [];

    const tableUsageData = stats?.tableUsageData?.map(item => ({
        table: item.table,
        hours: parseFloat(item.hours) || 0
    })) || [];

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-3xl font-semibold">Club Dashboard</h1>
                <p className="text-muted-foreground">
                    Overview of your billiards club operations
                </p>
                {user && (
                    <p className="text-sm text-muted-foreground mt-1">
                        Welcome back, {user.customerName}
                    </p>
                )}
            </div>

            {/* KPI Cards */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Revenue</CardTitle>
                        <DollarSign className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {formatCurrency(stats?.totalRevenue)}
                        </div>
                        <p className="text-xs text-muted-foreground">
                            {stats?.monthlyGrowth >= 0 ? '+' : ''}
                            {stats?.monthlyGrowth?.toFixed(1)}% from last month
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Tables</CardTitle>
                        <Table className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{stats?.totalTables || 0}</div>
                        <p className="text-xs text-muted-foreground">Billiard tables</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Employees</CardTitle>
                        <Users className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{stats?.totalEmployees || 0}</div>
                        <p className="text-xs text-muted-foreground">Total staff members</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Active Shifts</CardTitle>
                        <Calendar className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{stats?.activeShifts || 0}</div>
                        <p className="text-xs text-muted-foreground">Currently working</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Products</CardTitle>
                        <Package className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{stats?.totalProducts || 0}</div>
                        <p className="text-xs text-muted-foreground">Available items</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Growth</CardTitle>
                        <TrendingUp className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {stats?.monthlyGrowth >= 0 ? '+' : ''}
                            {stats?.monthlyGrowth?.toFixed(1)}%
                        </div>
                        <p className="text-xs text-muted-foreground">Monthly growth</p>
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
                        {revenueData.length > 0 ? (
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
                                        <XAxis dataKey="date" />
                                        <YAxis />
                                        <ChartTooltip content={<ChartTooltipContent />} />
                                        <Line
                                            type="monotone"
                                            dataKey="revenue"
                                            stroke="var(--chart-1)"
                                            strokeWidth={2}
                                        />
                                    </LineChart>
                                </ResponsiveContainer>
                            </ChartContainer>
                        ) : (
                            <div className="h-[200px] flex items-center justify-center text-muted-foreground">
                                No revenue data available
                            </div>
                        )}
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Table Usage</CardTitle>
                        <CardDescription>Hours played per table today</CardDescription>
                    </CardHeader>
                    <CardContent>
                        {tableUsageData.length > 0 ? (
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
                                        <Bar
                                            dataKey="hours"
                                            fill="var(--chart-2)"
                                            radius={[4, 4, 0, 0]}
                                        />
                                    </BarChart>
                                </ResponsiveContainer>
                            </ChartContainer>
                        ) : (
                            <div className="h-[200px] flex items-center justify-center text-muted-foreground">
                                No table usage data available
                            </div>
                        )}
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