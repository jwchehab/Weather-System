import { useEffect, useState } from "react";

const WEBSOCKET_URL = "ws://localhost:8080/api/alerts/ws"; // API Gateway WebSocket route

export const useWebSocket = () => {
    const [messages, setMessages] = useState<string[]>([]);
    const [socket, setSocket] = useState<WebSocket | null>(null);

    useEffect(() => {
        const ws = new WebSocket(WEBSOCKET_URL);

        ws.onopen = () => {
            console.log("WebSocket connected");
        };

        ws.onmessage = (event) => {
            console.log("WebSocket message received:", event.data);
            setMessages((prev) => [...prev, event.data]);
        };

        ws.onerror = (error) => {
            console.error("WebSocket error:", error);
        };

        ws.onclose = () => {
            console.log("WebSocket disconnected");
        };

        setSocket(ws);

        return () => {
            ws.close();
        };
    }, []);

    return { socket, messages };
};
