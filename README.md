# GoCode

App Android para aprender Python, Java, C++ y C con texto y ejemplos tipo terminal.
Se compila con Gradle y GitHub Actions (pestaña Actions > Build APK > Artifacts).

## Contenido

Todo sale de archivos JSON en `app/src/main/assets/`:

- `index.json`: lista de lenguajes (id, name, description, file, color).
- `python.json`, `java.json`, `cpp.json`, `c.json`, `shell.json`: niveles y guías.
- `luanti.json`: Lua | Minetest, dividido en rutas (`tracks`): Lua, Mod, Juego y Textura. Cada ruta tiene sus propios niveles y guías.

Formato de un lenguaje (si tiene `tracks`, cada ruta lleva `id`, `title`, `description`, `levels` y `guides`):

```
{ "levels": [ { "title", "description", "sections": [ { "title", "text", "examples": ["$ cat a.py\n..."], "note" } ] } ],
  "guides": [ { "title", "text", "examples", "note" } ] }
```

- En `text`, un párrafo nuevo se separa con una línea en blanco y `` `así` `` se ve como código.
- En `examples`, las líneas que empiezan con `$ ` se ven como comandos.
- Los niveles se desbloquean en orden.

Sin recompilar: copia tus JSON a `Android/data/com.craftplay777.gocode/files/content/` y la app los usa en vez de los incluidos.
