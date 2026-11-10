package com.craftplay777.gocode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {

    SharedPreferences prefs;
    JSONObject index;
    HashMap<String, JSONObject> cache = new HashMap<>();
    ArrayList<Runnable> stack = new ArrayList<>();
    int textSp = 16;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("gocode", MODE_PRIVATE);
        textSp = prefs.getInt("textSp", 16);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);
        index = readJson("index.json");
        push(this::home);
    }

    // Navegación
    void push(Runnable r) {
        stack.add(r);
        r.run();
    }

    void replaceTop(Runnable r) {
        stack.set(stack.size() - 1, r);
        r.run();
    }

    void back() {
        if (stack.size() > 1) {
            stack.remove(stack.size() - 1);
            stack.get(stack.size() - 1).run();
        } else {
            finish();
        }
    }

    void refresh() {
        stack.get(stack.size() - 1).run();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    // Carga de contenido: primero la carpeta externa, si no existe usa assets
    JSONObject readJson(String name) {
        try {
            File dir = getExternalFilesDir(null);
            if (dir != null) {
                File f = new File(dir, "content/" + name);
                if (f.exists()) {
                    return parse(new FileInputStream(f));
                }
            }
        } catch (Exception e) {
            // si el archivo externo está mal, se usa el de assets
        }
        try {
            return parse(getAssets().open(name));
        } catch (Exception e) {
            return null;
        }
    }

    JSONObject parse(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        in.close();
        return new JSONObject(out.toString("UTF-8"));
    }

    JSONObject lang(JSONObject l) {
        String file = l.optString("file");
        JSONObject data = cache.get(file);
        if (data == null) {
            data = readJson(file);
            if (data == null) data = new JSONObject();
            cache.put(file, data);
        }
        return data;
    }

    static JSONArray arr(JSONObject o, String k) {
        JSONArray a = o == null ? null : o.optJSONArray(k);
        return a == null ? new JSONArray() : a;
    }

    static JSONObject obj(JSONArray a, int i) {
        JSONObject o = a.optJSONObject(i);
        return o == null ? new JSONObject() : o;
    }

    static int color(String s, int def) {
        try {
            return Color.parseColor(s);
        } catch (Exception e) {
            return def;
        }
    }

    // Progreso
    String key(String lang, int lvl, int sec) {
        return "d_" + lang + "_" + lvl + "_" + sec;
    }

    int levelDoneCount(String lang, int lvl, int total) {
        int n = 0;
        for (int s = 0; s < total; s++) {
            if (prefs.getBoolean(key(lang, lvl, s), false)) n++;
        }
        return n;
    }

    boolean levelDone(String lang, JSONArray levels, int lvl) {
        int total = arr(obj(levels, lvl), "sections").length();
        return total > 0 && levelDoneCount(lang, lvl, total) == total;
    }

    boolean unlocked(String lang, JSONArray levels, int lvl) {
        return lvl == 0 || levelDone(lang, levels, lvl - 1);
    }

    // Marco de pantalla: barra arriba, contenido con scroll y barra opcional abajo
    void setScreen(String title, boolean showBack, View body, View bottom, boolean sizeButtons) {
        LinearLayout root = Ui.col(this);
        root.setBackgroundColor(Ui.BG);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(Ui.BAR);

        if (showBack) {
            TextView bk = Ui.text(this, "‹", 32, Ui.TEXT, true);
            bk.setPadding(Ui.dp(this, 16), 0, Ui.dp(this, 16), Ui.dp(this, 4));
            bk.setOnClickListener(v -> back());
            bar.addView(bk);
        }

        TextView t = Ui.text(this, title, 18, Ui.TEXT, true);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.END);
        if (!showBack) t.setPadding(Ui.dp(this, 16), 0, 0, 0);
        bar.addView(t, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));

        if (sizeButtons) {
            TextView menos = Ui.text(this, "A-", 16, Ui.SUB, true);
            menos.setPadding(Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12));
            menos.setOnClickListener(v -> cambiarTexto(-2));
            bar.addView(menos);
            TextView mas = Ui.text(this, "A+", 16, Ui.SUB, true);
            mas.setPadding(Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 16), Ui.dp(this, 12));
            mas.setOnClickListener(v -> cambiarTexto(2));
            bar.addView(mas);
        }

        root.addView(bar, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, 52)));

        ScrollView sv = new ScrollView(this);
        sv.addView(body);
        root.addView(sv, new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));

        if (bottom != null) {
            root.addView(bottom, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
        setContentView(root);
    }

    void cambiarTexto(int delta) {
        textSp = Math.max(12, Math.min(26, textSp + delta));
        prefs.edit().putInt("textSp", textSp).apply();
        refresh();
    }

    LinearLayout bodyCol() {
        LinearLayout body = Ui.col(this);
        int p = Ui.dp(this, 16);
        body.setPadding(p, p, p, p + Ui.dp(this, 16));
        return body;
    }

    // Pantalla 1: lenguajes
    void home() {
        LinearLayout body = bodyCol();
        body.addView(Ui.text(this,
                "Elige un lenguaje. Todo se explica con texto y ejemplos reales de terminal.",
                14, Ui.SUB, false));

        if (index == null) {
            Ui.add(body, Ui.text(this, "No se pudo cargar el contenido (index.json).", 15, 0xFFFF6B6B, false), 16);
            setScreen("GoCode", false, body, null, false);
            return;
        }

        JSONArray langs = arr(index, "languages");
        for (int i = 0; i < langs.length(); i++) {
            final JSONObject l = obj(langs, i);
            int accent = color(l.optString("color"), Ui.ACCENT);
            JSONArray levels = arr(lang(l), "levels");

            int total = 0;
            int done = 0;
            for (int v = 0; v < levels.length(); v++) {
                int n = arr(obj(levels, v), "sections").length();
                total += n;
                done += levelDoneCount(l.optString("id"), v, n);
            }

            LinearLayout card = Ui.card(this, true);
            card.addView(Ui.text(this, l.optString("name"), 22, accent, true));
            Ui.add(card, Ui.text(this, l.optString("description"), 14, Ui.TEXT, false), 4);
            Ui.add(card, Ui.text(this, done + " de " + total + " secciones", 13, Ui.SUB, false), 8);
            card.addView(Ui.bar(this, done, total, accent));
            card.setOnClickListener(v -> push(() -> langScreen(l)));
            Ui.add(body, card, 14);
        }

        TextView reset = Ui.button(this, "Reiniciar progreso", false);
        reset.setOnClickListener(v -> confirmarReset());
        Ui.add(body, reset, 24);

        File dir = getExternalFilesDir(null);
        String ruta = dir == null ? "" : dir.getAbsolutePath() + "/content/";
        String info = "El contenido está en archivos JSON. Para agregar o cambiar lenguajes, copia tus archivos a:\n" + ruta;
        Ui.add(body, Ui.text(this, info, 12, Ui.SUB, false), 16);

        setScreen("GoCode", false, body, null, false);
    }

    void confirmarReset() {
        new AlertDialog.Builder(this)
                .setTitle("Reiniciar progreso")
                .setMessage("Se vuelven a bloquear los niveles. ¿Seguro?")
                .setPositiveButton("Reiniciar", (d, w) -> {
                    SharedPreferences.Editor e = prefs.edit();
                    for (String k : prefs.getAll().keySet()) {
                        if (k.startsWith("d_")) e.remove(k);
                    }
                    e.apply();
                    refresh();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Pantalla 2: niveles y guías de un lenguaje
    void langScreen(final JSONObject l) {
        String id = l.optString("id");
        int accent = color(l.optString("color"), Ui.ACCENT);
        JSONObject data = lang(l);
        JSONArray levels = arr(data, "levels");
        JSONArray guides = arr(data, "guides");

        LinearLayout body = bodyCol();
        body.addView(Ui.text(this, "NIVELES", 12, Ui.SUB, true));

        for (int i = 0; i < levels.length(); i++) {
            final int li = i;
            JSONObject lv = obj(levels, i);
            int total = arr(lv, "sections").length();
            int done = levelDoneCount(id, i, total);
            boolean open = unlocked(id, levels, i);

            LinearLayout card = Ui.card(this, open);
            String title = (open ? "" : "🔒  ") + lv.optString("title");
            card.addView(Ui.text(this, title, 17, open ? Ui.TEXT : Ui.SUB, true));
            Ui.add(card, Ui.text(this, lv.optString("description"), 14, Ui.SUB, false), 4);

            String estado;
            if (!open) {
                estado = "Completa el nivel anterior para desbloquearlo";
            } else if (done == total && total > 0) {
                estado = "Completado ✔";
            } else {
                estado = done + " de " + total + " secciones";
            }
            Ui.add(card, Ui.text(this, estado, 13, open ? accent : Ui.SUB, false), 8);

            if (open) {
                card.setOnClickListener(v -> push(() -> levelScreen(l, li)));
            } else {
                card.setOnClickListener(v -> Toast.makeText(this,
                        "Completa el nivel anterior para desbloquear este", Toast.LENGTH_SHORT).show());
            }
            Ui.add(body, card, 10);
        }

        if (guides.length() > 0) {
            Ui.add(body, Ui.text(this, "GUÍAS (TERMUX Y SINTAXIS)", 12, Ui.SUB, true), 24);
            for (int g = 0; g < guides.length(); g++) {
                final int gi = g;
                JSONObject gd = obj(guides, g);
                LinearLayout card = Ui.card(this, true);
                card.addView(Ui.text(this, gd.optString("title"), 16, Ui.TEXT, true));
                card.setOnClickListener(v -> push(() -> section(l, -1, guides, gi)));
                Ui.add(body, card, 10);
            }
        }

        setScreen(l.optString("name"), true, body, null, false);
    }

    // Pantalla 3: secciones de un nivel
    void levelScreen(final JSONObject l, final int lvl) {
        String id = l.optString("id");
        int accent = color(l.optString("color"), Ui.ACCENT);
        JSONObject lv = obj(arr(lang(l), "levels"), lvl);
        final JSONArray secs = arr(lv, "sections");

        LinearLayout body = bodyCol();
        body.addView(Ui.text(this, lv.optString("description"), 14, Ui.SUB, false));

        for (int s = 0; s < secs.length(); s++) {
            final int si = s;
            boolean hecho = prefs.getBoolean(key(id, lvl, s), false);
            LinearLayout card = Ui.card(this, true);
            String t = (s + 1) + ".  " + obj(secs, s).optString("title") + (hecho ? "   ✔" : "");
            card.addView(Ui.text(this, t, 16, hecho ? accent : Ui.TEXT, true));
            card.setOnClickListener(v -> push(() -> section(l, lvl, secs, si)));
            Ui.add(body, card, 10);
        }

        setScreen(lv.optString("title"), true, body, null, false);
    }

    // Pantalla 4: una sección (texto + ejemplos de terminal). lvl = -1 para guías
    void section(final JSONObject l, final int lvl, final JSONArray secs, final int idx) {
        final String id = l.optString("id");
        JSONObject s = obj(secs, idx);

        LinearLayout body = bodyCol();
        body.addView(Ui.text(this, s.optString("title"), textSp + 6, Ui.TEXT, true));
        String sub = l.optString("name") + "  ·  " + (idx + 1) + " de " + secs.length();
        Ui.add(body, Ui.text(this, sub, 13, Ui.SUB, false), 2);

        String[] paras = s.optString("text").split("\n\n");
        for (String p : paras) {
            if (p.trim().isEmpty()) continue;
            TextView tv = Ui.text(this, Ui.rich(p.trim()), textSp, Ui.TEXT, false);
            tv.setLineSpacing(0f, 1.2f);
            Ui.add(body, tv, 14);
        }

        JSONArray exs = arr(s, "examples");
        for (int e = 0; e < exs.length(); e++) {
            String label = exs.length() > 1 ? "Ejemplo " + (e + 1) : "Ejemplo";
            Ui.add(body, Ui.text(this, label, 13, Ui.GREEN, true), 20);
            Ui.add(body, Ui.terminal(this, exs.optString(e), Math.max(11, textSp - 3)), 6);
        }

        String note = s.optString("note", "");
        if (!note.isEmpty()) {
            LinearLayout box = Ui.col(this);
            int p = Ui.dp(this, 12);
            box.setPadding(p, p, p, p);
            box.setBackground(Ui.box(this, 0xFF2A2415, 10));
            box.addView(Ui.text(this, Ui.rich(note), Math.max(13, textSp - 1), 0xFFFFD27A, false));
            Ui.add(body, box, 20);
        }

        // Barra de abajo
        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setBackgroundColor(Ui.BAR);
        int pad = Ui.dp(this, 12);
        bottom.setPadding(pad, pad, pad, pad);

        if (idx > 0) {
            TextView prev = Ui.button(this, "‹ Anterior", false);
            prev.setOnClickListener(v -> replaceTop(() -> section(l, lvl, secs, idx - 1)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.WRAP, 1f);
            lp.rightMargin = Ui.dp(this, 8);
            bottom.addView(prev, lp);
        }

        final boolean last = idx == secs.length() - 1;
        String label;
        if (lvl >= 0) {
            label = last ? "Terminar nivel ✔" : "Listo ✔  Siguiente ›";
        } else {
            label = last ? "Volver" : "Siguiente ›";
        }
        TextView next = Ui.button(this, label, true);
        next.setOnClickListener(v -> {
            if (lvl >= 0) prefs.edit().putBoolean(key(id, lvl, idx), true).apply();
            if (!last) {
                replaceTop(() -> section(l, lvl, secs, idx + 1));
            } else {
                terminarLista(l, lvl);
            }
        });
        bottom.addView(next, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));

        setScreen(l.optString("name"), true, body, bottom, true);
    }

    void terminarLista(JSONObject l, int lvl) {
        if (lvl >= 0) {
            String id = l.optString("id");
            JSONArray levels = arr(lang(l), "levels");
            if (levelDone(id, levels, lvl)) {
                if (lvl + 1 < levels.length()) {
                    Toast.makeText(this, "¡Nivel completado! Se desbloqueó el siguiente", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "¡Terminaste todos los niveles!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Aún te faltan secciones por marcar en este nivel", Toast.LENGTH_LONG).show();
            }
        }
        back();
    }
}
