(function () {

    const URL_CHECK_LOGIN = "CheckPassword";
    const URL_CHECK_REGISTRATION = "CheckRegistration"

    document.getElementById("LoginButton").addEventListener("click", (e) => {
        const form = e.target.closest("form");
        const userField = form.elements["username"];
        const pwdField = form.elements["password"];

        userField.style.borderColor = "";
        pwdField.style.borderColor = "";

        if (form.checkValidity()) {

            makeCall("POST", URL_CHECK_LOGIN, form,
                function (req) {
                    const message = req.responseText;
                    if (req.readyState === XMLHttpRequest.DONE) {
                        if (req.status === 200) {
                            //saving username in session and redirect to another page
                            sessionStorage.setItem('username', message);
                            window.location.href = "homePage.html"
                        } else if (req.status === 401) {//incorrect credentials
                            userField.style.borderColor = "red";
                            pwdField.style.borderColor = "red";
                            document.getElementById("errorMessageLogin").textContent = message;
                        } else {
                            document.getElementById("errorMessageLogin").textContent = message;
                        }
                    }
                });
        } else {
            form.reportValidity();
        }
    });

    document.getElementById("RegistratonButton").addEventListener("click", (e) => {
        const form = e.target.closest("form");
        const pwdField = form.elements["newPassword"];
        const rpt_pwdField = form.elements["newRepeatedPassword"];

        pwdField.style.borderColor = "";
        rpt_pwdField.style.borderColor = "";

        if (pwdField.value === rpt_pwdField.value) {

            if (form.checkValidity()) {
                makeCall("POST", URL_CHECK_REGISTRATION, form,
                    function (req) {
                        const message = req.responseText;
                        if (req.readyState === XMLHttpRequest.DONE) {
                            if (req.status === 200) {
                                document.getElementById("errorMessageRegistration").textContent = "You have successfully registered , please log in";
                            } else {
                                document.getElementById("errorMessageRegistration").textContent = message;
                            }
                        }
                    });
            } else {
                form.reportValidity();
            }
        } else {
            e.preventDefault();
            document.getElementById("errorMessageRegistration").textContent = "Passwords do not match.";
            pwdField.style.borderColor = "red";
            rpt_pwdField.style.borderColor = "red";
        }
    });
})()