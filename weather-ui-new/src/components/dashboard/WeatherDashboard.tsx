import React, { useState, useEffect } from 'react';
import { format, addDays } from 'date-fns';
import { weatherService, WeatherReport } from '../../services/weatherService';
import AlertSection from './AlertSection';
import StatisticsSection from './StatisticsSection';
import axios from "axios";
import AlertDialogDemo from "../ui/alert-dialog"

interface LandingViewProps {
    onViewChange: (view: 'landing' | 'forecast') => void;
}

interface ForecastViewProps {
    onBack: () => void;
}

const LandingView: React.FC<LandingViewProps> = ({ onViewChange }) => {
    const [cacheSize, setCacheSize] = useState<number | null>(null);
    const [clearingCache, setClearingCache] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchCacheSize = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/storage/cache/size');
            const size = await response.json();

            console.log('Fetched cache size:', size);

            setCacheSize(size);
        } catch (err) {
            console.error('Error fetching cache size:', err);
            setError('Failed to fetch cache size');
        }
    };

    const clearCache = async () => {
        try {
            setClearingCache(true);
            await fetch('http://localhost:8080/api/storage/cache/clear', {
                method: 'POST'
            });
            await fetchCacheSize();
            setClearingCache(false);
        } catch (err) {
            console.error('Error clearing cache:', err);
            setError('Failed to clear cache');
            setClearingCache(false);
        }
    };

    useEffect(() => {
        fetchCacheSize();
    }, []);

    return (
        <div className="bg-white shadow-lg rounded-lg p-8">
            <h1 className="text-4xl font-bold text-center mb-12 text-gray-800">Weather Dashboard</h1>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-12">
                {/* Forecast Section */}
                <div
                    onClick={() => onViewChange('forecast')}
                    className="flex flex-col items-center cursor-pointer transform transition-transform hover:scale-105"
                >
                    <div className="bg-blue-50 border-2 border-blue-200 p-6 mb-4 rounded-lg w-full">
                        <div className="w-16 h-16 mx-auto flex items-center justify-center">
                            <svg className="w-10 h-10 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z" />
                            </svg>
                        </div>
                    </div>
                    <h2 className="text-xl font-semibold text-gray-800">Forecast</h2>
                    <p className="text-center text-gray-600 mt-2">
                        View weather forecasts<br/>and create reports
                    </p>
                </div>

                {/* Route Section */}
                <div className="flex flex-col items-center">
                    <div className="bg-green-50 border-2 border-green-200 p-6 mb-4 rounded-lg w-full">
                        <div className="w-16 h-16 mx-auto flex items-center justify-center">
                            <svg className="w-10 h-10 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
                            </svg>
                        </div>
                    </div>
                    <h2 className="text-xl font-semibold text-gray-800">Route</h2>
                    <p className="text-center text-gray-600 mt-2">
                        Explore and plan<br/>travel conditions
                    </p>
                </div>

                {/* Archives Section */}
                <div className="flex flex-col items-center">
                    <div className="bg-purple-50 border-2 border-purple-200 p-6 mb-4 rounded-lg w-full">
                        <div className="w-16 h-16 mx-auto flex items-center justify-center">
                            <svg className="w-10 h-10 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
                            </svg>
                        </div>
                    </div>
                    <h2 className="text-xl font-semibold text-gray-800">Archives</h2>
                    <p className="text-center text-gray-600 mt-2">
                        View saved reports<br/>and routes
                    </p>
                </div>
            </div>

            {/* Cache Management Section */}
            <div className="mt-12 p-6 bg-gray-50 rounded-lg">
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-lg font-semibold text-gray-800">Data Cache</h3>
                    <AlertDialogDemo onClearCache={clearCache} clearingCache={clearingCache} />
                </div>
                <div className="text-gray-600">
                    <p className="mb-2">
                        Weather data is cached locally at C:\Users\Username\Documents\WeatherApp
                        for faster access. Temporary data is cached up to 100 Mb.
                        <br></br>
                        Current cache size: {cacheSize ?? '...'} MB
                    </p>
                    <p className="text-sm text-gray-500">
                        Note: Weather data is stored locally and may not reflect real-time conditions. Clear cache to fetch fresh data.
                    </p>
                    {error && (
                        <p className="mt-2 text-red-500">{error}</p>
                    )}
                </div>
            </div>
        </div>
    );
};

const ForecastView: React.FC<ForecastViewProps> = ({ onBack }) => {
    const [location, setLocation] = useState('');
    const [selectedDate, setSelectedDate] = useState(new Date());

    const fetchWeatherData = async () => {
        if (!location) return;

        try {
            const formattedDate = format(selectedDate, 'yyyy-MM-dd');
            console.log('Fetching forecast for:', location, formattedDate);

            const weeklyForecast = await weatherService.getWeeklyForecast(
                location,
                formattedDate
            );
            console.log('Response received:', weeklyForecast);

            setWeeklyData(weeklyForecast);
        } catch (error) {
            console.error('Error details:', error);
            if (axios.isAxiosError(error)) {
                console.error('Response:', error.response?.data);
                console.error('Status:', error.response?.status);
            }
        }
    };

    const weekDays = [...Array(7)].map((_, index) => {
        const date = addDays(selectedDate, index);
        return {
            full: format(date, 'EEE'), // 'EEE' gives us short day name (Mon, Tue, etc.)
            date: date,
        };
    });

    const [weeklyData, setWeeklyData] = useState<WeatherReport[]>([]);

    return (
        <div className="bg-white shadow-lg rounded-lg p-6">
            {/* Back Button */}
            <button
                onClick={onBack}
                className="mb-6 flex items-center gap-2 text-gray-600 hover:text-gray-800"
            >
                ← Back
            </button>

            {/* Search Section */}
            <div className="flex gap-4 mb-8">
                <input
                    type="text"
                    value={location}
                    onChange={(e) => setLocation(e.target.value)}
                    placeholder="Enter location..."
                    className="flex-1 px-4 py-2 border rounded-lg"
                />
                <input
                    type="date"
                    value={format(selectedDate, 'yyyy-MM-dd')}
                    onChange={(e) => {
                        // avoid timezone issues by setting time to noon
                        const date = new Date(e.target.value + 'T12:00:00');
                        setSelectedDate(date);
                    }}
                    className="px-4 py-2 border rounded-lg"
                />
                <button
                    onClick={fetchWeatherData}
                    className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
                >
                    Search
                </button>
            </div>

            {/* Forecast Panel */}
            <div className="grid grid-cols-7 gap-4 p-4">
                {weekDays.map((day, index) => {
                    const weatherData = weeklyData[index];
                    return (
                        <div
                            key={index}
                            className="bg-gray-100 p-4 rounded-lg shadow-sm"
                        >
                            <div className="text-center space-y-2">
                                <div className="font-medium text-gray-800">
                                    {format(day.date, 'MMM d')}
                                </div>
                                <div className="text-sm text-gray-600">
                                    {day.full}
                                </div>
                                {weatherData && (
                                    <>
                                        <div className="text-2xl font-bold text-gray-900">
                                            {weatherData.highTemp}°
                                        </div>
                                        <div className="text-sm text-gray-600">
                                            {weatherData.lowTemp}°
                                        </div>
                                        <div className="flex items-center justify-center gap-1 text-sm text-gray-600">
                                            <svg
                                                className={'w-4 h-4'}
                                                fill='currentColor'
                                                viewBox="0 0 24 24"
                                                xmlns="http://www.w3.org/2000/svg"
                                            >
                                                <path
                                                    d="M12 2C9.791 2 8 5.023 8 8.5C8 12.35 12 16 12 16C12 16 16 12.35 16 8.5C16 5.023 14.209 2 12 2ZM12 14C10.619 14 9.5 12.881 9.5 11.5C9.5 10.119 10.619 9 12 9C13.381 9 14.5 10.119 14.5 11.5C14.5 12.881 13.381 14 12 14Z"
                                                />
                                            </svg>
                                            <span>{weatherData.precipitationChance}%</span>
                                        </div>
                                        <div className="flex flex-col items-center text-sm text-gray-600">
                                            <svg className="w-4 h-4 transform rotate-45" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 10l7-7m0 0l7 7m-7-7v18" />
                                            </svg>
                                            <span>{weatherData.windSpeed} km/h</span>
                                        </div>
                                    </>
                                )}
                            </div>
                        </div>
                    );
                })}
            </div>

            <StatisticsSection initialLocation={location}
                               initialStartDate={selectedDate} />
            <AlertSection />
        </div>
    );
};

const WeatherDashboard: React.FC = () => {
    const [view, setView] = useState<'landing' | 'forecast'>('landing');

    return (
        <div className="min-h-screen bg-gray-100">
            <div className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
                {view === 'landing' ? (
                    <LandingView onViewChange={setView} />
                ) : (
                    <ForecastView onBack={() => setView('landing')} />
                )}
            </div>
        </div>
    );
};

export default WeatherDashboard;