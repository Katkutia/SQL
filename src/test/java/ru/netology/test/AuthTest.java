package ru.netology.test;

import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.data.SQLHelper;
import ru.netology.pages.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static ru.netology.data.SQLHelper.cleanAuthCodes;
import static ru.netology.data.SQLHelper.cleanDatabase;

class AuthTest {


    LoginPage loginPage;


    @AfterEach
    void tearDown() {
        cleanAuthCodes();
    }


    @AfterAll
    static void tearDownAll() {
        cleanDatabase();
    }


    @BeforeEach
    void setUp() {
        loginPage = open("http://localhost:9999", LoginPage.class);
    }

    @Test
    @DisplayName("Позитивный тест")
    void shouldSuccessfulLogin() {
        var authInfo = DataHelper.getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        verificationPage.veryfyVerificationPageVisiblity();
        var verificationCode = SQLHelper.getVerificationCode();
        verificationPage.validVerify(verificationCode.getCode());
    }

    @Test
    @DisplayName("Неверно указано имя пользователя")
    void shouldErrorInvalideLogin() {
        var authInfo = DataHelper.generateRandomUser();
        loginPage.validLogin(authInfo);
        loginPage.verifyErrorNotification("Ошибка! Неверно указан логин или пароль");
    }

    @Test
    @DisplayName("Неверно указан код верификации")
    void shouldInvalidCodes() {
        var authInfo = DataHelper.getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        verificationPage.veryfyVerificationPageVisiblity();
        var verificationCode = DataHelper.generateRandomVerificationCode();
        verificationPage.verify(verificationCode.getCode());
        verificationPage.verifyErrorNotification("Ошибка! Неверно указан код! Попробуйте ещё раз.");
    }

    @Test
    @DisplayName("Ввод неправильного пароля 3 раза")
    void shouldLockAfterThreeUnsuccessfulPasswords() {
        var authInfo = DataHelper.generateRandomUser();
        var verificationPage = loginPage.validLogin(authInfo);
        verificationPage.verifyErrorNotification("Ошибка! Неверно указан логин или пароль");
        for (int count = 0; count < 3; count++) {
            loginPage.validLogin(DataHelper.generateRandomUser());
        }
        verificationPage.verifyErrorNotification("Превышено максимальное количество попыток авторизации");
    }
}