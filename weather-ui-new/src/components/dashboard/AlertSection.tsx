import React, { useState, useEffect } from "react";
import { Alert, AlertRequest, Condition, alertService } from "../../services/alertService";
import { useWebSocket } from "../../hooks/useWebSocket";

const AlertSection = () => {
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [conditions, setConditions] = useState<Condition[]>([
        { parameter: "", operator: "", threshold: 0 }
    ]);
    const [combinator, setCombinator] = useState<"AND" | "OR">("AND");
    const [location, setLocation] = useState<{ latitude: number; longitude: number } | null>(null); // New state for location

    const { messages } = useWebSocket(); // WebSocket Hook to receive live updates

    useEffect(() => {
        // Fetch location on component mount
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    setLocation({
                        latitude: position.coords.latitude,
                        longitude: position.coords.longitude,
                    });
                },
                (error) => {
                    console.error("Geolocation error:", error);
                    setError("Failed to retrieve location");
                }
            );
        } else {
            setError("Geolocation is not supported by this browser.");
        }
    }, []);

    useEffect(() => {
        fetchAlerts();
    }, []);

    useEffect(() => {
        if (messages.length > 0) {
            console.log("New WebSocket alert received:", messages[messages.length - 1]);
            fetchAlerts(); // Refresh alerts when a new WebSocket message arrives
        }
    }, [messages]);

    const fetchAlerts = async () => {
        try {
            setLoading(true);
            const activeAlerts = await alertService.getActiveAlerts();
            setAlerts(activeAlerts);
        } catch (err) {
            setError("Failed to fetch alerts");
            console.error("Error fetching alerts:", err);
        } finally {
            setLoading(false);
        }
    };

    const handleConditionChange = (index: number, field: keyof Condition, value: string | number) => {
        const updatedConditions = [...conditions];
        updatedConditions[index] = {
            ...updatedConditions[index],
            [field]: field === "threshold" ? Number(value) : value
        };
        setConditions(updatedConditions);
    };

    const addCondition = () => {
        setConditions([...conditions, { parameter: "", operator: "", threshold: 0 }]);
    };

    const removeCondition = (index: number) => {
        if (conditions.length > 1) {
            setConditions(conditions.filter((_, i) => i !== index));
        }
    };

    const handleSaveAlert = async () => {
        if (conditions.some(c => !c.parameter || !c.operator)) {
            setError("Please fill in all condition fields");
            return;
        }

        if (!location) {
            setError("Unable to retrieve location.");
            return;
        }

        setLoading(true);
        setError(null);
        try {
            const request: AlertRequest = {
                conditions,
                combinator,
                location: { latitude: location.latitude, longitude: location.longitude }, // Pass location here
            };
            await alertService.createAlert(request);
            await fetchAlerts();

            // Reset form
            setConditions([{ parameter: "", operator: "", threshold: 0 }]);
        } catch (err) {
            setError("Failed to save alert");
            console.error("Error saving alert:", err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <h3 className="font-semibold mb-4">Live Alerts:</h3>

            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                    {error}
                </div>
            )}

            {/* Live Alerts via WebSocket */}
            {messages.length > 0 && (
                <div className="mb-6">
                    <h4 className="text-sm font-medium mb-2">New Alerts:</h4>
                    <div className="space-y-2">
                        {messages.map((msg, idx) => (
                            <div key={idx} className="p-3 bg-green-100 border border-green-400 rounded-lg">
                                {msg}
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Active Alerts from API */}
            {alerts.length > 0 && (
                <div className="mb-6">
                    <h4 className="text-sm font-medium mb-2">Active Alerts:</h4>
                    <div className="space-y-2">
                        {alerts.map((alert) => (
                            <div key={alert.id} className="flex items-center justify-between p-3 bg-blue-50 rounded-lg">
                                <div className="flex-1">
                                    {alert.conditions.map((condition, idx) => (
                                        <span key={idx}>
                                            {idx > 0 && (
                                                <span className="mx-2 text-gray-500">{alert.combinator}</span>
                                            )}
                                            <span className="text-gray-700">
                                                {condition.parameter} {condition.operator} {condition.threshold}
                                            </span>
                                        </span>
                                    ))}
                                </div>
                                <button
                                    onClick={async () => {
                                        try {
                                            await alertService.updateAlertStatus(alert.id, false);
                                            await fetchAlerts();
                                        } catch (err) {
                                            setError("Failed to disable alert");
                                        }
                                    }}
                                    className="ml-4 text-red-500 hover:text-red-700"
                                >
                                    Disable
                                </button>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* New Alert Form */}
            <div className="space-y-4">
                {/* Combinator Selection */}
                <div className="flex justify-center gap-4 mb-4">
                    <button
                        onClick={() => setCombinator("AND")}
                        className={`px-4 py-2 rounded-lg transition-colors ${
                            combinator === "AND" ? "bg-blue-500 text-white" : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                        }`}
                    >
                        Match ALL conditions (AND)
                    </button>
                    <button
                        onClick={() => setCombinator("OR")}
                        className={`px-4 py-2 rounded-lg transition-colors ${
                            combinator === "OR" ? "bg-blue-500 text-white" : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                        }`}
                    >
                        Match ANY condition (OR)
                    </button>
                </div>

                {/* Conditions */}
                {conditions.map((condition, index) => (
                    <div key={index} className="flex gap-4 items-start">
                        <div className="flex-1 grid grid-cols-3 gap-4">
                            <select
                                value={condition.parameter}
                                onChange={(e) => handleConditionChange(index, "parameter", e.target.value)}
                                className="w-full border rounded-lg p-2"
                            >
                                <option value="">Select Parameter</option>
                                <option value="temperature">Temperature</option>
                                <option value="precipitation">Precipitation</option>
                                <option value="wind">Wind Speed</option>
                                <option value="humidity">Humidity</option>
                            </select>

                            <select
                                value={condition.operator}
                                onChange={(e) => handleConditionChange(index, "operator", e.target.value)}
                                className="w-full border rounded-lg p-2"
                            >
                                <option value="">Select Operator</option>
                                <option value=">">&gt;</option>
                                <option value="<">&lt;</option>
                                <option value="=">=</option>
                            </select>

                            <input
                                type="number"
                                value={condition.threshold !== undefined ? condition.threshold : ""}
                                onChange={(e) => handleConditionChange(index, "threshold", e.target.value)}
                                placeholder="Value"
                                className="w-full border rounded-lg p-2"
                            />
                        </div>

                        {conditions.length > 1 && (
                            <button
                                onClick={() => removeCondition(index)}
                                className="text-red-500 hover:text-red-700 p-2"
                                title="Remove condition"
                            >
                                ✕
                            </button>
                        )}
                    </div>
                ))}

                <div className="flex justify-between">
                    <button onClick={addCondition} className="text-blue-500 hover:text-blue-700">
                        + Add Condition
                    </button>

                    <button
                        onClick={handleSaveAlert}
                        disabled={loading || conditions.some(c => !c.parameter || !c.operator)}
                        className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-blue-300 transition-colors"
                    >
                        {loading ? "Saving..." : "Save Alert"}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AlertSection;
