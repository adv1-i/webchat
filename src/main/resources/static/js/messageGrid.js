function adjustMessageLayout() {
    const messages = document.querySelectorAll(".message");
    messages.forEach((message) => {
        const content = message.querySelector(".message-content");
        const imagesContainer = message.querySelector(".message-images");
        const images = imagesContainer ? imagesContainer.querySelectorAll("img") : [];

        Promise.all(
            Array.from(images).map((img) => {
                if (img.complete) return Promise.resolve();
                return new Promise((resolve) => {
                    img.onload = img.onerror = resolve;
                });
            })
        ).then(() => {
            if (images.length > 0 && imagesContainer) {
                let columns;
                if (images.length === 1) columns = 1;
                else if (images.length <= 4) columns = 2;
                else if (images.length <= 9) columns = 3;
                else columns = 4;

                imagesContainer.style.gridTemplateColumns = `repeat(${columns}, 1fr)`;
            }

            if (content && message.parentElement) {
                const maxWidth = message.parentElement.offsetWidth * 0.7;
                const contentWidth = content.offsetWidth;

                if (contentWidth < maxWidth) {
                    message.style.width = "auto";
                } else {
                    message.style.width = "70%";
                }
            }
        });
    });
}
window.addEventListener("load", () => setTimeout(adjustMessageLayout, 100));
window.addEventListener("resize", adjustMessageLayout);
function adjustLayoutAfterNewMessage() {
    setTimeout(adjustMessageLayout, 0);
}
