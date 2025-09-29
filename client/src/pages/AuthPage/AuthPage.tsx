import React from "react";
import styles from "./AuthPage.module.scss";
import { Auth } from "../../components/Auth";
import { SpriteIcon } from "../../components/SpriteIcon/SpriteIcon";

const AuthPage: React.FC = () => {
  return (
    <div className={styles.auth}>
      <div className={styles.auth__contentInner}>
        <div className={styles.auth__logo}>
          <SpriteIcon
            className={styles.navMenu__logo}
            name="logo-skilldrones"
            width={320}
            height={130}
          />
        </div>
        <Auth />
      </div>
      <div className={styles.auth__imgContainer}>
        <div className={styles.auth__img}></div>
      </div>
    </div>
  );
};

export default AuthPage;
