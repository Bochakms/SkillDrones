import { Link, NavLink } from "react-router-dom";
import styles from "./NavMenu.module.scss";

import { SpriteIcon } from "../SpriteIcon/SpriteIcon";
import { getNavRoutes } from "../../utils/routeUtils";

export const NavMenu = () => {
  const userName = "Иванов Иван";
  const userRole = "admin";
  // const user = useAppSelector(selectUser);
  // const userRole = user?.role || "guest";

  const navRoutes = getNavRoutes(userRole);

  return (
    <nav className={styles.navMenu}>
      <div className={styles.navMenu__leftWrapper}>
        <Link to={"/"}>
          <SpriteIcon
            className={styles.navMenu__logo}
            name="logo-skilldrones-secondary"
            width={64}
            height={64}
          />
        </Link>
        {navRoutes.map((route) => (
          <NavLink
            key={route.path}
            to={route.path}
            className={({ isActive }) =>
              `${styles.navMenu__pageLink} ${
                isActive ? styles["navMenu__pageLink--active"] : ""
              }`
            }
          >
            {route.title}
          </NavLink>
        ))}
      </div>
      <div className={styles.navMenu__rightWrapper}>
        <div className={styles.navMenu__iconWrapper}>
          <SpriteIcon name="icon-bell" width={32} height={32} />
          <SpriteIcon name="icon-menu" width={32} height={32} />
        </div>
        <NavLink to="/account" className={styles.navMenu__pageLink}>
          <div className={styles.accCard}>
            <img
              className={styles.accCard__avatar}
              src={"/image/ava.png"}
              alt="avatar"
            />
            <div className={styles.accCard__wrapper}>
              <div className={styles.accCard__userName}>{userName}</div>
              <div className={styles.accCard__userRole}>{userRole}</div>
            </div>
          </div>
        </NavLink>
      </div>
    </nav>
  );
};
