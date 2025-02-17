import React, { useState } from 'react';
import { format } from 'date-fns';
import { weatherService, WeatherStatistics } from '../../services/weatherService';

interface StatisticsSectionProps {
    initialLocation: string;
    initialStartDate: Date;
}

const StatisticsSection: React.FC<StatisticsSectionProps> = ({
                                                                 initialLocation,
                                                                 initialStartDate
                                                             }) => {
    const [selectedMetrics, setSelectedMetrics] = useState<Set<string>>(new Set());
    const [statistics, setStatistics] = useState<WeatherStatistics | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [location, setLocation] = useState(initialLocation);
    const [startDate, setStartDate] = useState(initialStartDate);
    const [endDate, setEndDate] = useState(new Date());

    const metrics = [
        { id: 'temps', label: 'Temps' },
        { id: 'precipitate', label: 'Precipitate' },
        { id: 'wind', label: 'Wind' },
        { id: 'humidity', label: 'Humidity' }
    ];

    const handleMetricToggle = (metricId: string) => {
        const newMetrics = new Set(selectedMetrics);
        if (newMetrics.has(metricId)) {
            newMetrics.delete(metricId);
        } else {
            newMetrics.add(metricId);
        }
        setSelectedMetrics(newMetrics);
    };

    const handleReset = () => {
        setSelectedMetrics(new Set());
        setStatistics(null);
        setLocation(initialLocation);
        setStartDate(initialStartDate);
        setEndDate(new Date());
    };

    const handleGenerate = async () => {
        if (!location || selectedMetrics.size === 0) {
            setError('Please select at least one metric and provide a location');
            return;
        }

        if (endDate < startDate) {
            setError('End date must be after start date');
            return;
        }

        setLoading(true);
        setError(null);
        try {
            const stats = await weatherService.calculateStatistics({
                location,
                startDate: format(startDate, 'yyyy-MM-dd'),
                endDate: format(endDate, 'yyyy-MM-dd'),
                metrics: Array.from(selectedMetrics)
            });
            setStatistics(stats);
        } catch (error) {
            setError('Error generating statistics');
            console.error('Error generating statistics:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="mb-8">
            <h3 className="font-semibold mb-4">Statistics:</h3>

            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                    {error}
                </div>
            )}

            <div className="grid grid-cols-2 gap-8">
                {/* Left Column - Date Range and Location */}
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Location
                        </label>
                        <input
                            type="text"
                            value={location}
                            onChange={(e) => setLocation(e.target.value)}
                            className="w-full border rounded-lg p-2"
                            placeholder="Enter location"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Start Date
                        </label>
                        <input
                            type="date"
                            value={format(startDate, 'yyyy-MM-dd')}
                            onChange={(e) => setStartDate(new Date(e.target.value))}
                            className="w-full border rounded-lg p-2"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            End Date
                        </label>
                        <input
                            type="date"
                            value={format(endDate, 'yyyy-MM-dd')}
                            onChange={(e) => setEndDate(new Date(e.target.value))}
                            className="w-full border rounded-lg p-2"
                        />
                    </div>
                </div>

                {/* Right Column - Metrics Selection */}
                <div>
                    <div className="border rounded-lg p-4">
                        <div className="space-y-2">
                            {metrics.map(metric => (
                                <label key={metric.id} className="flex items-center gap-2">
                                    <input
                                        type="checkbox"
                                        checked={selectedMetrics.has(metric.id)}
                                        onChange={() => handleMetricToggle(metric.id)}
                                        className="rounded"
                                    />
                                    <span>{metric.label}</span>
                                </label>
                            ))}
                        </div>
                    </div>

                    <div className="mt-4 space-y-2">
                        <button
                            onClick={handleReset}
                            className="w-full border rounded-lg px-4 py-2 hover:bg-gray-50"
                        >
                            Reset
                        </button>
                        <button
                            onClick={handleGenerate}
                            disabled={loading || selectedMetrics.size === 0}
                            className="w-full bg-blue-500 text-white rounded-lg px-4 py-2 hover:bg-blue-600 disabled:bg-blue-300"
                        >
                            {loading ? 'Generating...' : 'Generate'}
                        </button>
                    </div>
                </div>
            </div>

            {/* Statistics Results */}
            {statistics && (
                <div className="mt-6 p-4 bg-gray-50 rounded-lg">
                    <h4 className="font-medium mb-2">Results for {statistics.location}:</h4>
                    <div className="grid grid-cols-2 gap-4">
                        {selectedMetrics.has('temps') && (
                            <div>Average Temperature: {statistics.averageTemperature.toFixed(1)}°C</div>
                        )}
                        {selectedMetrics.has('precipitate') && (
                            <div>Average Precipitation: {statistics.averagePrecipitation.toFixed(1)}%</div>
                        )}
                        {selectedMetrics.has('wind') && (
                            <div>Average Wind Speed: {statistics.averageWindSpeed.toFixed(1)} km/h</div>
                        )}
                        {selectedMetrics.has('humidity') && (
                            <div>Average Humidity: {statistics.averageHumidity.toFixed(1)}%</div>
                        )}
                    </div>
                    <div className="mt-2 text-sm text-gray-600">
                        Period: {format(new Date(statistics.startDate), 'MMM d, yyyy')} - {format(new Date(statistics.endDate), 'MMM d, yyyy')}
                    </div>
                </div>
            )}
        </div>
    );
};

export default StatisticsSection;