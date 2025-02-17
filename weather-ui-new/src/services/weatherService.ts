import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

export interface WeatherReport {
    location: string;
    date: string;
    highTemp: number;
    lowTemp: number;
    humidity: number;
    windSpeed: number;
    precipitationChance: number;
}

export interface WeatherStatistics {
    location: string;
    startDate: string;
    endDate: string;
    averageTemperature: number;
    averagePrecipitation: number;
    averageWindSpeed: number;
    averageHumidity: number;
    calculated: string;
}

export interface StatisticsRequest {
    location: string;
    startDate: string;
    endDate: string;
    metrics: string[];
}

export const weatherService = {
    getWeatherReport: async (location: string, date: string): Promise<WeatherReport> => {
        const response = await axios.get(`${API_BASE_URL}/weather/report`, {
            params: { location, date }
        });
        return response.data;
    },

    getWeeklyForecast: async (location: string, startDate: string): Promise<WeatherReport[]> => {
        const response = await axios.get(`${API_BASE_URL}/weather/forecast`, {
            params: { location, startDate }
        });
        return response.data;
    },

    calculateStatistics: async (request: StatisticsRequest): Promise<WeatherStatistics> => {
        try {
            const response = await axios.post(`${API_BASE_URL}/statistics`, request);
            return response.data;
        } catch (error) {
            console.error('Error calculating statistics:', error);
            throw new Error('Failed to calculate statistics');
        }
    }
};