import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';
const WS_URL = 'ws://localhost:8080/api/alerts/ws'; // WebSocket URL

export interface Condition {
    parameter: string;
    operator: string;
    threshold: number;
}

export interface Alert {
    id: string;
    active: boolean;
    conditions: Condition[];
    combinator: 'AND' | 'OR';
    created: string;
}

export interface AlertRequest {
    conditions: Condition[];
    combinator: 'AND' | 'OR';
    location: { latitude: number; longitude: number };
}

export interface AlertNotification {
    id: string;
    alertId: string;
    message: string;
    timestamp: string;
    acknowledged: boolean;
}

export const alertService = {
    createAlert: async (request: AlertRequest): Promise<Alert> => {
        const response = await axios.post(`${API_BASE_URL}/alerts`, request);
        return response.data;
    },

    getActiveAlerts: async (): Promise<Alert[]> => {
        const response = await axios.get(`${API_BASE_URL}/alerts`);
        return response.data;
    },

    updateAlertStatus: async (alertId: string, active: boolean): Promise<void> => {
        await axios.put(`${API_BASE_URL}/alerts/${alertId}/status`, null, {
            params: { active }
        });
    },

    getNotifications: async (): Promise<AlertNotification[]> => {
        const response = await axios.get(`${API_BASE_URL}/alerts/notifications`);
        return response.data;
    },

    // WebSocket Setup
    connectToAlerts: (onMessage: (notification: AlertNotification) => void): WebSocket => {
        const socket = new WebSocket(WS_URL);

        socket.onopen = () => console.log('Connected to WebSocket');
        socket.onmessage = (event) => {
            try {
                const notification: AlertNotification = JSON.parse(event.data);
                onMessage(notification);
            } catch (error) {
                console.error('Error parsing WebSocket message', error);
            }
        };

        socket.onclose = () => console.log('Disconnected from WebSocket');
        socket.onerror = (error) => console.error('WebSocket error:', error);

        return socket;
    }
};
