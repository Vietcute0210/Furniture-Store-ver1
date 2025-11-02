(function () {
  const FORM_ACTION_PREFIX = "/add-product-to-cart";
  const DUR_TO_CENTER = 500;
  const PAUSE_AT_CENTER = 100;
  const DUR_TO_CART = 1000;

  // helpers
  const $ = (s, r = document) => r.querySelector(s);
  const clamp = (v, a, b) => Math.min(b, Math.max(a, v));
  const lerp = (a, b, t) => a + (b - a) * t;

  function qBezier(p0, p1, p2, t) {
    const u = 1 - t;
    return {
      x: u * u * p0.x + 2 * u * t * p1.x + t * t * p2.x,
      y: u * u * p0.y + 2 * u * t * p1.y + t * t * p2.y,
    };
  }

  function findCartIcon() {
    return (
      $(".container-fluid.fixed-top .fa-shopping-bag") || $(".fa-shopping-bag")
    );
  }
  function findCartBadge() {
    const icon = findCartIcon();
    const a = icon && icon.closest("a");
    return a
      ? a.querySelector(".position-absolute.bg-secondary, .bg-secondary")
      : null;
  }
  function nearestProductImage(el) {
    const root =
      el.closest(
        ".fruite-item, .fruite-img, .product, .card, [data-product]"
      ) || el.closest("div,li,section,article");
    return root ? root.querySelector("img") : null;
  }
  function injectStyles() {
    if ($("#cart-fly-styles")) return;
    const css = `
      .fly-clone{position:fixed;z-index:3000;pointer-events:none;border-radius:50%;object-fit:cover;
        box-shadow:0 12px 28px rgba(0,0,0,.22);will-change:transform,opacity}
      .cart-bump{transform-origin:center;transition:transform .28s ease;transform:scale(1.12) rotate(-6deg)}
      .cart-plus-anim{position:fixed;z-index:3500;pointer-events:none;font-weight:600;color:#fff;background:#ff4d4f;
        padding:4px 7px;border-radius:12px;font-size:12px;opacity:0;transform:translateY(0) scale(.85);
        transition:transform .6s cubic-bezier(.22,.61,.36,1),opacity .6s}
    `;
    const s = document.createElement("style");
    s.id = "cart-fly-styles";
    s.textContent = css;
    document.head.appendChild(s);
  }

  function flyToCart(startEl, imgUrl, done) {
    const cart = findCartIcon();
    if (!cart) {
      done && done();
      return;
    }
    injectStyles();

    // create clone
    const r0 = startEl.getBoundingClientRect();
    const clone = document.createElement("img");
    clone.src = imgUrl;
    clone.alt = "";
    clone.className = "fly-clone";
    const base = clamp(
      Math.min(r0.width, r0.height) * 1.1,
      80,
      window.innerHeight * 0.22
    );

    Object.assign(clone.style, {
      left: `${r0.left + r0.width / 2 - base / 2}px`,
      top: `${r0.top + r0.height / 2 - base / 2}px`,
      width: `${base}px`,
      height: `${base}px`,
      opacity: "0.95",
      transition: `transform ${DUR_TO_CENTER}ms cubic-bezier(.23,1,.32,1),opacity ${DUR_TO_CENTER}ms`,
    });
    document.body.appendChild(clone);

    // Phase 1: move to center & shrink a chút
    const cx = window.innerWidth / 2,
      cy = window.innerHeight * 0.4;
    requestAnimationFrame(() => {
      const tx = cx - (r0.left + r0.width / 2);
      const ty = cy - (r0.top + r0.height / 2);
      clone.style.transform = `translate(${tx}px, ${ty}px) scale(.8)`;
    });

    // Phase 2: Bézier curve to cart after pause
    setTimeout(() => {
      const rC = cart.getBoundingClientRect();
      const p0 = { x: cx, y: cy };
      const p2 = { x: rC.left + rC.width / 2, y: rC.top + rC.height / 2 };
      const mid = { x: (p0.x + p2.x) / 2, y: (p0.y + p2.y) / 2 };
      const dx = p2.x - p0.x,
        dy = p2.y - p0.y;
      const len = Math.hypot(dx, dy) || 1;
      // vector pháp tuyến để bẻ cong (ngửa lên trên)
      const nx = -dy / len,
        ny = dx / len;
      const bend = clamp(len * 0.35, 120, 260); // độ cong
      const p1 = {
        x: mid.x + nx * bend,
        y: mid.y + ny * bend - Math.abs(bend * 0.25),
      };

      // animate along Bézier with rAF
      const t0 = performance.now();
      const startW = base,
        endScale = 0.18;

      function step(now) {
        const t = clamp((now - t0) / DUR_TO_CART, 0, 1);
        const e = t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
        const pt = qBezier(p0, p1, p2, e);
        const s = lerp(1, endScale, e);
        clone.style.transition = "none";
        clone.style.transform = `translate(${
          pt.x - (r0.left + r0.width / 2)
        }px, ${pt.y - (r0.top + r0.height / 2)}px) scale(${s})`;
        clone.style.opacity = String(lerp(0.95, 0.35, e));
        if (t < 1) requestAnimationFrame(step);
        else finish();
      }

      function finish() {
        clone.remove();
        cart.classList.add("cart-bump");
        setTimeout(() => cart.classList.remove("cart-bump"), 420);
        const badge = findCartBadge();
        if (badge) {
          const n =
            parseInt((badge.textContent || "0").replace(/\D/g, "")) || 0;
          badge.textContent = String(n + 1);
        } else {
          const plus = document.createElement("div");
          plus.className = "cart-plus-anim";
          plus.textContent = "+1";
          document.body.appendChild(plus);
          const rc = cart.getBoundingClientRect();
          plus.style.left = `${rc.left + rc.width / 2 - 18}px`;
          plus.style.top = `${rc.top + rc.height / 2 - 12}px`;
          requestAnimationFrame(() => {
            plus.style.opacity = "1";
            plus.style.transform = "translateY(-18px) scale(1)";
          });
          setTimeout(() => {
            plus.style.opacity = "0";
            plus.style.transform = "translateY(-32px) scale(.8)";
            setTimeout(() => plus.remove(), 520);
          }, 520);
        }
        done && done();
      }

      requestAnimationFrame(step);
    }, DUR_TO_CENTER + PAUSE_AT_CENTER);
  }

  function onClick(e) {
    const btn = e.target.closest("button, a");
    if (!btn) return;
    const form = btn.closest("form");
    if (!form) return;
    const action = form.getAttribute("action") || "";
    if (!action.startsWith(FORM_ACTION_PREFIX)) return;

    const img = nearestProductImage(btn);
    const src = img && (img.currentSrc || img.src);
    if (!src) return; // không có ảnh: để submit mặc định

    e.preventDefault();
    flyToCart(img || btn, src, () =>
      setTimeout(() => {
        try {
          form.submit();
        } catch {}
      }, 60)
    );
  }

  const attach = () => document.addEventListener("click", onClick);
  document.readyState === "loading"
    ? document.addEventListener("DOMContentLoaded", attach)
    : attach();
})();
