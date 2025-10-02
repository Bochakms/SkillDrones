import { BrowserRouter, Route, Routes, Navigate } from "react-router-dom";
import { Layout } from "./components/Layout";
import { NavMenu } from "./components/NavMenu";
import { Loader } from "./components/Loader/Loader";
import styles from "./App.module.scss";
import { useAppSelector } from "./app/hooks";
import { selectAuthLoading } from "./app/authSlice";
import AppRouter from "./components/AppRouter/AppRouter";
import { getPublicRoutes } from "./utils/routeUtils";

function App() {
  // const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const isLoading = useAppSelector(selectAuthLoading);

  const isAuthenticated = true;

  if (isLoading) {
    return <Loader />;
  }

  if (!isAuthenticated) {
    const publicRoutes = getPublicRoutes();

    return (
      <BrowserRouter>
        <div className={styles.app}>
          <Routes>
            {publicRoutes.map((route) => (
              <Route
                key={route.path}
                path={route.path}
                element={<route.component />}
              />
            ))}
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </div>
      </BrowserRouter>
    );
  }

  return (
    <BrowserRouter>
      <header className={styles.header}>
        <Layout>
          <NavMenu />
        </Layout>
      </header>

      <main className={styles.main}>
        <Layout>
          <AppRouter />
        </Layout>
      </main>
    </BrowserRouter>
  );
}

export default App;
