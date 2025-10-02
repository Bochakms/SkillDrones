
import { useState, useCallback } from 'react';
import { flightsApi } from '../api/flightsApi';
import type { FlightsState, FlightsFilter } from '../types/flightTypes';
import type { Region } from '../types/regionTypes';

export const useFlights = () => {
  const [state, setState] = useState<FlightsState>({
    flights: [],
    loading: false,
    error: null,
    lastUpdated: null
  });

  const [regions, setRegions] = useState<Region[]>([]);
  const [regionsLoading, setRegionsLoading] = useState(false);
  const [validationErrors, setValidationErrors] = useState<string[]>([]);

  const fetchFlights = useCallback(async (filters?: FlightsFilter) => {
    setState(prev => ({ ...prev, loading: true, error: null }));
    setValidationErrors([]);

    try {
      // Валидация фильтров перед отправкой
      if (filters) {
        const validation = flightsApi.validateFilters(filters);
        if (!validation.isValid) {
          setValidationErrors(validation.errors);
          throw new Error('Ошибка валидации фильтров');
        }
      }

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

  const fetchRegions = useCallback(async () => {
    setRegionsLoading(true);
    try {
      const regionsData = await flightsApi.getRegions();
      setRegions(regionsData);
    } catch (error) {
      console.error('Ошибка загрузки регионов:', error);
    } finally {
      setRegionsLoading(false);
    }
  }, []);

  const clearError = useCallback(() => {
    setState(prev => ({ ...prev, error: null }));
    setValidationErrors([]);
  }, []);

  const clearFlights = useCallback(() => {
    setState({
      flights: [],
      loading: false,
      error: null,
      lastUpdated: null
    });
    setValidationErrors([]);
  }, []);

  return {
    // Состояние полетов
    flights: state.flights,
    loading: state.loading,
    error: state.error,
    lastUpdated: state.lastUpdated,
    
    // Состояние регионов
    regions,
    regionsLoading,
    
    // Ошибки валидации
    validationErrors,
    
    // Методы
    fetchFlights,
    fetchRegions,
    clearError,
    clearFlights
  };
};