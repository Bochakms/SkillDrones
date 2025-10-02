import { Routes, Route, Navigate } from "react-router-dom";
import { getProtectedRoutes } from "../../utils/routeUtils";
import RegionPage from "../../pages/RegionPage/RegionPage";
// import { selectUser } from "../../app/authSlice";
// import { useAppSelector } from "../../app/hooks";

function AppRouter() {
  //   const user = useAppSelector(selectUser);
  //   const userRole = user?.role || "guest";
  const userRole = "admin";

  const protectedRoutes = getProtectedRoutes(userRole);

  return (
    <Routes>
      {protectedRoutes.map((route) => (
        <Route
          key={route.path}
          path={route.path}
          element={<route.component />}
        />
      ))}

      <Route path="/region/:regionId" element={<RegionPage />} />

      {/* Реддирект с корневого пути */}
      <Route path="/" element={<Navigate to="/" replace />} />

      {/* Обработка несуществующих маршрутов */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default AppRouter;
