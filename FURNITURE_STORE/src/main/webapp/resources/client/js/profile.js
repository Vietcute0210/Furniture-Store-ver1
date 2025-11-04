// // profile.js — avatar uploader only (minimal)
// (function () {
//   const MAX_SIZE = 300 * 1024; // 300KB
//   const ACCEPT = ["image/jpeg", "image/png", "image/webp"];

//   function bytesToKB(b) {
//     return Math.round(b / 1024);
//   }

//   function setupUploader(input) {
//     if (!input || input.dataset.enhanced === "1") return;
//     input.classList.add("uploader-input"); // hide native input via CSS

//     // Wrapper
//     const wrapper = document.createElement("div");
//     wrapper.className = "avatar-uploader";

//     // Dropzone with plus sign
//     const dz = document.createElement("div");
//     dz.className = "uploader-dropzone";

//     // Preview image
//     const preview = document.createElement("img");
//     preview.className = "uploader-preview";
//     preview.alt = "avatar preview";

//     // Edit button (replaces the old clear button).  Use a pencil icon via FontAwesome if loaded.
//     const editBtn = document.createElement("button");
//     editBtn.type = "button";
//     editBtn.className = "uploader-edit";
//     // Prefer a font-awesome icon when available; fallback to Unicode pencil
//     editBtn.innerHTML = '<i class="fas fa-pen"></i>';

//     dz.appendChild(preview);
//     dz.appendChild(editBtn);

//     // Hint + error (optional, compact)
//     const hint = document.createElement("div");
//     hint.className = "uploader-hint";
//     hint.innerHTML = "Tối đa <b>300KB</b> • Gợi ý <b>500×650px</b>";

//     const err = document.createElement("div");
//     err.className = "uploader-error";

//     // Insert before input, then move input into wrapper's right column
//     input.parentNode.insertBefore(wrapper, input);
//     wrapper.appendChild(dz);
//     const rightCol = document.createElement("div");
//     rightCol.appendChild(input);
//     rightCol.appendChild(hint);
//     rightCol.appendChild(err);
//     wrapper.appendChild(rightCol);

//     // Initial avatar url from JSP (optional)
//     const initialUrl =
//       input.getAttribute("data-current-url") ||
//       (document.getElementById("currentAvatar") &&
//         document.getElementById("currentAvatar").getAttribute("src"));
//     if (initialUrl) {
//       preview.src = initialUrl;
//       preview.style.display = "block";
//       // show edit button when there is an existing avatar
//       editBtn.style.display = "flex";
//       dz.classList.add("has-preview");
//     }

//     // Click to open file picker
//     dz.addEventListener("click", () => input.click());

//     // Drag & drop
//     ["dragenter", "dragover"].forEach((evt) =>
//       dz.addEventListener(evt, (e) => {
//         e.preventDefault();
//         dz.classList.add("drag");
//       })
//     );
//     ["dragleave", "drop"].forEach((evt) =>
//       dz.addEventListener(evt, (e) => {
//         e.preventDefault();
//         dz.classList.remove("drag");
//       })
//     );
//     dz.addEventListener("drop", (e) => {
//       const file =
//         e.dataTransfer && e.dataTransfer.files && e.dataTransfer.files[0];
//       if (file) handleFile(file);
//     });

//     // Change from input
//     input.addEventListener("change", () => {
//       const file = input.files && input.files[0];
//       if (file) handleFile(file);
//     });

//     // Edit behaviour: open the native file picker without clearing the current preview.
//     editBtn.addEventListener("click", (e) => {
//       e.stopPropagation();
//       input.click();
//     });

//     // Accessibility: ensure edit button contains a visible fallback icon when FA isn't loaded
//     // If the button currently contains only an <i> and FontAwesome isn't present, append a text fallback.
//     const hasIcon = editBtn.querySelector("i");
//     if (!hasIcon) {
//       editBtn.textContent = "✎";
//     } else {
//       // also insert fallback span so CSS can show/hide if needed
//       const fb = document.createElement("span");
//       fb.className = "uploader-edit-fallback";
//       fb.textContent = "✎";
//       editBtn.appendChild(fb);
//     }

//     function handleFile(file) {
//       if (!ACCEPT.includes(file.type)) {
//         err.textContent = "Chỉ nhận JPG/PNG/WebP";
//         err.style.display = "block";
//         return;
//       }
//       if (file.size > MAX_SIZE) {
//         err.textContent = `Ảnh quá lớn (${bytesToKB(
//           file.size
//         )}KB) — tối đa 300KB`;
//         err.style.display = "block";
//         return;
//       }
//       err.style.display = "none";
//       const url = URL.createObjectURL(file);
//       preview.src = url;
//       preview.style.display = "block";
//       // show the edit button when a preview is present
//       editBtn.style.display = "flex";
//       dz.classList.add("has-preview");
//     }

//     input.dataset.enhanced = "1";
//   }

//   document.addEventListener("DOMContentLoaded", function () {
//     setupUploader(document.getElementById("avatarFile"));
//   });
// })();
// profile.js — avatar upload enhancement
// This script attaches custom behaviour to avatar upload components with the
// class .avatar-uploader-custom. It provides an intuitive, modern
// experience for selecting a new avatar: clicking on the avatar frame or
// the pencil button opens the file picker, previews the selected image,
// and displays any errors.

(function () {
  // Maximum allowed file size (300 KB)
  const MAX_SIZE = 300 * 1024;
  // Allowed MIME types
  const ACCEPTED_TYPES = ["image/jpeg", "image/png", "image/webp"];

  function setupAvatarUploader(container) {
    const fileInput = container.querySelector('input[type="file"]');
    const previewImg = container.querySelector(".avatar-preview");
    const hintEl = container.querySelector(".avatar-hint");
    const errorEl = container.querySelector(".avatar-error");
    const editBtn = container.querySelector(".edit-avatar-btn");
    const frame = container.querySelector(".uploader-frame");

    if (!fileInput || !previewImg || !frame) return;

    // Show initial avatar if provided via data-current-url
    const initialUrl = fileInput.getAttribute("data-current-url");
    if (initialUrl) {
      previewImg.src = initialUrl;
      previewImg.style.display = "block";
    }

    // Trigger file selection when clicking on frame or edit button
    const openFilePicker = () => {
      fileInput.click();
    };
    frame.addEventListener("click", openFilePicker);
    if (editBtn) {
      editBtn.addEventListener("click", function (e) {
        e.preventDefault();
        e.stopPropagation();
        openFilePicker();
      });
    }

    // Handle file selection
    fileInput.addEventListener("change", function () {
      const file = fileInput.files && fileInput.files[0];
      if (!file) return;

      // Validate MIME type
      if (!ACCEPTED_TYPES.includes(file.type)) {
        if (errorEl) {
          errorEl.textContent = "Chỉ nhận hình JPG/PNG/WebP";
          errorEl.style.display = "block";
        }
        return;
      }

      // Validate file size
      if (file.size > MAX_SIZE) {
        if (errorEl) {
          const kb = Math.round(file.size / 1024);
          const maxKb = Math.round(MAX_SIZE / 1024);
          errorEl.textContent = `Ảnh quá lớn (${kb}KB) — tối đa ${maxKb}KB`;
          errorEl.style.display = "block";
        }
        return;
      }

      // Clear any previous error
      if (errorEl) errorEl.style.display = "none";

      // Preview the selected image
      const url = URL.createObjectURL(file);
      previewImg.src = url;
      previewImg.style.display = "block";
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    const uploaders = document.querySelectorAll(".avatar-uploader-custom");
    uploaders.forEach(setupAvatarUploader);
  });
})();
