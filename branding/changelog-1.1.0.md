Nothing new in the game. Angelus now runs on more versions of it.

- **Minecraft 1.21.1** on NeoForge, as before, and now on Fabric.
- **Minecraft 1.20.1, 1.19.2, 1.18.2 and 1.16.5** on Forge and on Fabric.
- **Fabric API** is needed on Fabric. Forge and NeoForge need nothing besides the loader.
- **One jar per pair**, named `angelus-<loader>-<minecraft>-1.1.0.jar`.
- On Fabric before 1.21.1 the block always reaches the vanilla distance, 4.5 blocks or 5 in creative, whatever your reach is. Elsewhere it follows your reach, as before.

The block keeps its id, its name, its texture and its recipe, so a world built on 1.0.1 opens unchanged.

---

ゲームの中身は変わっていません。動く Minecraft のバージョンが増えました。

- **Minecraft 1.21.1** は、これまでの NeoForge に加えて Fabric でも動きます。
- **Minecraft 1.20.1・1.19.2・1.18.2・1.16.5** は Forge と Fabric で動きます。
- Fabric では **Fabric API** が必要です。Forge と NeoForge はローダーのほかに何もいりません。
- **jar は組み合わせごとに1つ**で、名前は `angelus-<ローダー>-<Minecraft>-1.1.0.jar` です。
- 1.21.1 より前の Fabric では、設置できる距離はリーチにかかわらず本体の決まった距離（4.5 ブロック、クリエイティブでは 5）です。それ以外では、これまでどおりリーチに合わせて変わります。

ブロックの id・名前・テクスチャ・レシピはどれも変わっていないので、1.0.1 で作ったワールドはそのまま開けます。
