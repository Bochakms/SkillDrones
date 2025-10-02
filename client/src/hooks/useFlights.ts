// hooks/useFlights.ts
import { useState, useCallback } from 'react';
import { flightsApi } from '../api/flightsApi';
import type { FlightsState, FlightsFilter } from '../types/flightTypes';

export const useFlights = () => {
  const [state, setState] = useState<FlightsState>({
    flights: [],
    loading: false,
    error: null,
    lastUpdated: null
  });

  const fetchFlights = useCallback(async (filters?: FlightsFilter) => {
    setState(prev => ({ ...prev, loading: true, error: null }));

    try {
      const flights = await flightsApi.getFlights(filters);
      
      setState({
        flights,
        loading: false,
        error: null,
        lastUpdated: new Date()
      });

    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Неизвестная ошибка';
      setState(prev => ({
        ...prev,
        loading: false,
        error: errorMessage
      }));
    }
  }, []);

  const clearError = useCallback(() => {
    setState(prev => ({ ...prev, error: null }));
  }, []);

  const clearFlights = useCallback(() => {
    setState({
      flights: [],
      loading: false,
      error: null,
      lastUpdated: null
    });
  }, []);

  return {
    // Состояние полетов
    flights: state.flights,
    loading: state.loading,
    error: state.error,
    lastUpdated: state.lastUpdated,
    
    // Методы
    fetchFlights,
    clearError,
    clearFlights
  };
};