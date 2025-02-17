import * as AlertDialog from '@radix-ui/react-alert-dialog';
import React from 'react';

const AlertDialogDemo = ({ onClearCache, clearingCache }: { onClearCache: () => void, clearingCache: boolean }) => (
    <AlertDialog.Root>
        <AlertDialog.Trigger asChild>
            <button
                disabled={clearingCache}
                className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:bg-red-300 transition-colors"
            >
                {clearingCache ? 'Clearing...' : 'Clear Cache'}
            </button>
        </AlertDialog.Trigger>
        <AlertDialog.Portal>
            <AlertDialog.Overlay className="fixed inset-0 bg-black/50" />
            <AlertDialog.Content className="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-white p-6 rounded-lg w-[90vw] max-w-md">
                <AlertDialog.Title className="text-lg font-bold mb-2">
                    Are you absolutely sure?
                </AlertDialog.Title>
                <AlertDialog.Description className="text-gray-600 mb-4">
                    This will permanently delete your cached weather data. You will need to fetch new data from the weather service. This may result in slower app loading times.
                </AlertDialog.Description>
                <div className="flex justify-end gap-4">
                    <AlertDialog.Cancel asChild>
                        <button className="px-4 py-2 border rounded-lg hover:bg-gray-50">
                            Cancel
                        </button>
                    </AlertDialog.Cancel>
                    <AlertDialog.Action asChild>
                        <button
                            onClick={onClearCache}
                            className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600"
                        >
                            Clear Cache
                        </button>
                    </AlertDialog.Action>
                </div>
            </AlertDialog.Content>
        </AlertDialog.Portal>
    </AlertDialog.Root>
);

export default AlertDialogDemo;