function makeCall(method, url, formElement, cback, reset = true) {
    const req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        cback(req)
    };

    req.open(method, url);

    if (formElement == null) {
        req.send();
    } else {
        console.log("sending...")
        req.send(new FormData(formElement));
        if (reset) {
            formElement.reset();
        }
    }
}