import { ROUTES } from '../constants/routesConst';
import { ROLES } from '../constants/rolesConst';
import type { RouteConfig } from '../types/routeTypes';

import MainPage from '../pages/MainPage/MainPage';
import AccountPage from '../pages/AccountPage/AccountPage';
import AnalyticsPage from '../pages/AnalyticsPage/AnalyticsPage';
import ArchivePage from '../pages/ArchivePage/ArchivePage';
import UnauthorizedPage from '../pages/UnauthorizedPage/UnauthorizedPage';
import AuthPage from '../pages/AuthPage/AuthPage';
import AdminPage from '../pages/AdminPage/AdminPage';
import UploadPage from '../pages/UploadPage/UploadPage';


export const routeConfig: RouteConfig[] = [
  // Публичные маршруты
  {
    path: ROUTES.LOGIN,
    component: AuthPage,
    allowedRoles: [ROLES.OPERATOR, ROLES.ANALYST, ROLES.ADMIN],
    title: 'Вход',
    isPublic: true,
    hideFromNav: true
  },
  {
    path: ROUTES.UNAUTH,
    component: UnauthorizedPage,
    allowedRoles: [ROLES.OPERATOR, ROLES.ANALYST, ROLES.ADMIN],
    title: 'Доступ запрещен',
    isPublic: true,
    hideFromNav: true
  },
  
  // Защищенные маршруты
  {
    path: ROUTES.MAIN,
    component: MainPage,
    allowedRoles: [ROLES.OPERATOR, ROLES.ANALYST, ROLES.ADMIN],
    title: 'Главная',
    exact: true
  },
  {
    path: ROUTES.ACCOUNT,
    component: AccountPage,
    allowedRoles: [ROLES.OPERATOR, ROLES.ANALYST, ROLES.ADMIN],
    title: 'Аккаунт'
  },
  {
    path: ROUTES.ANALYTICS,
    component: AnalyticsPage,
    allowedRoles: [ROLES.OPERATOR, ROLES.ANALYST, ROLES.ADMIN],
    title: 'Аналитика'
  },
  {
    path: ROUTES.ARCHIVE,
    component: ArchivePage,
    allowedRoles: [ROLES.OPERATOR, ROLES.ADMIN],
    title: 'Архив'
  },
  {
    path: ROUTES.UPLOAD,
    component: UploadPage,
    allowedRoles: [ROLES.OPERATOR, ROLES.ADMIN],
    title: 'Загрузка данных'
  },
  {
    path: ROUTES.ADMIN,
    component: AdminPage,
    allowedRoles: [ROLES.ADMIN],
    title: 'Админ панель'
  }
];