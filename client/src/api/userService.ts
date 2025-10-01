import axios from 'axios';
import z from 'zod';
import { authUtils } from '../utils/authUtils';

export const LoginResponseSchema = z.object({
  result: z.boolean(),
});

export const LogoutResponseSchema = z.object({
  result: z.boolean(),
});

export const RegisterResponseSchema = z.object({
  success: z.boolean(),
});

export const RegisterErrorResponseSchema = z.object({
  error: z.string(),
});

export const UserProfileSchema = z.object({
  email: z.string().email(),
  name: z.string(),
  surname: z.string(),
  reports: z.array(z.string()),
});

export type LoginResponse = z.infer<typeof LoginResponseSchema>;
export type LogoutResponse = z.infer<typeof LogoutResponseSchema>;
export type RegisterResponse = z.infer<typeof RegisterResponseSchema>;
export type RegisterErrorResponse = z.infer<typeof RegisterErrorResponseSchema>;
export type UserProfile = z.infer<typeof UserProfileSchema>;

const API_BASE_URL = import.meta.env.API_BASE_URL || 'https://';

const authApi = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
  },
  withCredentials: true,
});

authApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const status = error.response.status;
      const message = error.response.data?.error || error.response.data?.message || 'Unknown error';
      
      switch (status) {
        case 400:
          throw new Error(`Ошибка входа`);
        case 401:
          authUtils.clearAuthData();
          throw new Error(`Unauthorized: ${message}`);
        case 403:
          throw new Error(`Forbidden: ${message}`);
        case 404:
          throw new Error(`Not found: ${message}`);
        case 409:
          throw new Error(`Пользователь уже существует`);
        default:
          throw new Error(`Server error: ${status} - ${message}`);
      }
    } else if (error.request) {
      throw new Error('Network error: No response received');
    } else {
      throw new Error(`Request error: ${error.message}`);
    }
  }
);

export const authService = {
  login: async (email: string, password: string): Promise<LoginResponse> => {
    const formData = new URLSearchParams();
    formData.append('email', email);
    formData.append('password', password);
    
    const response = await authApi.post('/auth/login', formData, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });

    return LoginResponseSchema.parse(response.data);
  },

  logout: async (): Promise<LogoutResponse> => {
    const response = await authApi.get('/auth/logout');
    authUtils.clearAuthData();
    return LogoutResponseSchema.parse(response.data);;
  },

  register: async (
    email: string, 
    password: string, 
    name: string, 
    surname: string
  ): Promise<RegisterResponse> => {
    const formData = new URLSearchParams();
    formData.append('email', email);
    formData.append('password', password);
    formData.append('name', name);
    formData.append('surname', surname);
    
    const response = await authApi.post('/user', formData, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });

    return RegisterResponseSchema.parse(response.data);
  },

  getProfile: async (): Promise<UserProfile> => {
  const response = await authApi.get('/profile');
  return UserProfileSchema.parse(response.data);
  }

};

