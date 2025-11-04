document.addEventListener("DOMContentLoaded", function () {
  // Avatar preview and click-to-select enhancement
  const avatarFile = document.getElementById("avatarFile");
  const avatarPreview = document.getElementById("avatarPreview");
  const currentAvatar = document.getElementById("currentAvatar");

  function showPreview(file) {
    if (!file) return;
    const allowed = ["image/png", "image/jpeg", "image/jpg"];
    if (!allowed.includes(file.type)) {
      console.warn("Unsupported file type for avatar preview");
      return;
    }
    const url = URL.createObjectURL(file);
    // Replace currentAvatar in-place so only one image is visible
    if (currentAvatar) {
      currentAvatar.src = url;
      currentAvatar.style.transition =
        "opacity 180ms ease, transform 180ms ease";
      currentAvatar.style.opacity = 0.0;
      requestAnimationFrame(() => {
        currentAvatar.style.opacity = 1;
        currentAvatar.style.transform = "scale(1.01)";
      });
    }
    // hide preview img element if exists
    if (avatarPreview) {
      avatarPreview.style.display = "none";
    }
  }

  if (avatarFile) {
    avatarFile.addEventListener("change", function (e) {
      const f = e.target.files && e.target.files[0];
      if (!f) return;
      // file size guard (5MB)
      if (f.size > 5 * 1024 * 1024) {
        alert("File quá lớn. Vui lòng chọn ảnh dưới 5MB.");
        avatarFile.value = "";
        return;
      }
      showPreview(f);
    });
  }

  // If user clicks on current avatar image on update page, open file selector
  if (currentAvatar && avatarFile) {
    currentAvatar.style.cursor = "pointer";
    currentAvatar.title = "Click để chọn ảnh mới";
    currentAvatar.addEventListener("click", () => avatarFile.click());
  }

  // Small enhancement for profile page: make profile images lift on hover
  document.querySelectorAll(".profile-img").forEach((img) => {
    img.addEventListener(
      "mouseover",
      () => (img.style.transform = "translateY(-6px) scale(1.02)")
    );
    img.addEventListener(
      "mouseout",
      () => (img.style.transform = "translateY(0) scale(1)")
    );
  });

  /* ------- Mask sensitive info on view profile ------- */
  function maskPhone(p) {
    // keep first 2 and last 2 digits
    const s = (p || "").toString();
    if (s.length <= 4) return "****";
    const first = s.slice(0, 2);
    const last = s.slice(-2);
    return first + "*".repeat(Math.max(3, s.length - 4)) + last;
  }
  function maskEmail(e) {
    if (!e) return "";
    const parts = e.split("@");
    if (parts.length < 2) return e.replace(/(.{2}).+(.{2})/, "$1****$2");
    const local = parts[0];
    const domain = parts[1];
    const keep = Math.min(2, local.length);
    return local.slice(0, keep) + "****@" + domain;
  }

  // find list items and replace email/phone text with masked versions and reveal buttons
  document.querySelectorAll(".profile-details li").forEach((li) => {
    const txt = li.textContent || "";
    if (txt.toLowerCase().includes("email")) {
      const raw = txt.split(":")[1]
        ? txt.split(":").slice(1).join(":").trim()
        : "";
      const masked = maskEmail(raw);
      li.innerHTML =
        '<span><strong>Email:</strong></span> <span class="masked-value">' +
        masked +
        '</span> <button class="reveal-btn" data-type="email" data-value="' +
        encodeURIComponent(raw) +
        '">Xem</button>';
    }
    if (
      txt.toLowerCase().includes("số điện thoại") ||
      txt.toLowerCase().includes("số điện thoại".toLowerCase()) ||
      txt.toLowerCase().includes("phone")
    ) {
      const raw = txt.split(":")[1]
        ? txt.split(":").slice(1).join(":").trim()
        : "";
      const masked = maskPhone(raw);
      li.innerHTML =
        '<span><strong>Số điện thoại:</strong></span> <span class="masked-value">' +
        masked +
        '</span> <button class="reveal-btn" data-type="phone" data-value="' +
        encodeURIComponent(raw) +
        '">Xem</button>';
    }
  });

  // modal helpers
  function buildModal() {
    const overlay = document.createElement("div");
    overlay.className = "reveal-modal-overlay";
    const modal = document.createElement("div");
    modal.className = "reveal-modal";
    modal.innerHTML =
      '<h4>Xác thực mật khẩu</h4><input type="password" placeholder="Nhập mật khẩu hiện tại" id="revealPwd" /><div class="actions"><button class="cancel">Hủy</button><button class="confirm">Xác nhận</button></div>';
    overlay.appendChild(modal);
    document.body.appendChild(overlay);
    // handlers
    overlay.querySelector(".cancel").addEventListener("click", () => {
      overlay.remove();
    });
    overlay.querySelector(".confirm").addEventListener("click", async () => {
      const pwd = modal.querySelector("#revealPwd").value;
      if (!pwd) {
        alert("Vui lòng nhập mật khẩu");
        return;
      }
      // attempt to verify with server endpoint; if not available, inform user
      try {
        const res = await fetch("/verify-password", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ password: pwd }),
        });
        if (res.ok) {
          const j = await res.json();
          if (j && j.valid) {
            overlay.dataset.verified = "1";
            overlay.dispatchEvent(new CustomEvent("verified"));
            overlay.remove();
            return;
          }
        }
        // try alternative endpoint
        const res2 = await fetch("/check-password", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ password: pwd }),
        });
        if (res2.ok) {
          const j2 = await res2.json();
          if (j2 && j2.valid) {
            overlay.dataset.verified = "1";
            overlay.dispatchEvent(new CustomEvent("verified"));
            overlay.remove();
            return;
          }
        }
        alert(
          "Mật khẩu không đúng hoặc server không hỗ trợ xác thực từ client."
        );
      } catch (err) {
        console.warn("verify error", err);
        alert(
          "Không thể xác minh mật khẩu — endpoint server chưa được cài. Vui lòng đăng nhập lại để xem thông tin."
        );
      }
    });
    return overlay;
  }

  // click handler for reveal buttons
  document.body.addEventListener("click", function (e) {
    const b = e.target.closest(".reveal-btn");
    if (!b) return;
    const type = b.dataset.type;
    const raw = decodeURIComponent(b.dataset.value || "");
    // open modal and wait for verified event
    const modal = buildModal();
    modal.addEventListener(
      "verified",
      () => {
        // reveal the value in place of masked-value
        const span = b.parentElement.querySelector(".masked-value");
        if (span) span.textContent = raw;
        b.remove();
      },
      { once: true }
    );
  });

  /* ------- Button micro-effect for update/back buttons ------- */
  document
    .querySelectorAll("button.btn-primary, a.btn-primary")
    .forEach((btn) => {
      // Add pressed class on mousedown / touchstart so user sees the jump *before* navigation/submit
      const press = (e) => {
        try {
          btn.classList.add("pressed");
        } catch (__) {}
        setTimeout(() => {
          try {
            btn.classList.remove("pressed");
          } catch (__) {}
        }, 220);
      };
      btn.addEventListener("mousedown", press);
      btn.addEventListener("touchstart", press, { passive: true });
      // fallback: also animate on click
      btn.addEventListener("click", (ev) => {
        try {
          btn.classList.add("pressed");
        } catch (__) {}
        setTimeout(() => {
          try {
            btn.classList.remove("pressed");
          } catch (__) {}
        }, 220);
      });
    });
});
