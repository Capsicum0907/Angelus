# Angelus

[English](README.md) | 日本語

空中に設置できるブロックを1つ追加します。目線の先に何もない状態で右クリックすると目の前にブロックが現れます。
即座に壊せてインベントリに入ります。

- 設置できる距離は普通のブロックと同じです。
- 水・背の高い草・雪の中にも置けます。
- 枠の中は見通せます。
- 羽根4枚と棒4本で1つ作れます。
- 設定項目はありません。

## スクリーンショット

![崖から空中へ](branding/gallery/01-out-over-nothing.png)

他の絵は [branding/gallery](branding/gallery) にあります。説明は
[captions.md](branding/gallery/captions.md) です。

## 対象

| | |
|---|---|
| Minecraft | 1.21.1 |
| ローダー | NeoForge 21.1.248 |
| Java | 21 |

## ビルド

```
run.bat                         # コンパイルして開発クライアントを起動
gradlew build                   # jar を作る
gradlew runGameTestServer       # ゲームテストを実行
gradlew runData                 # ブロックステート・モデル・レシピ・言語ファイルを作り直す
python tools/make_textures.py   # ブロックのテクスチャを作り直す
python tools/autopilot.py <ノード> --world <ワールド>   # 開発クライアントで確認を自動で行って終了
```

autopilot は `src/autopilot` にある開発専用の Mod で、`runAutopilot` タスクだけが読み込み、jar には入りません。
指定したワールドを開いて空中にブロックを置き、1つ壊し、結果を
`versions/<ノード>/autopilot-run/autopilot/result.txt` に PASS/FAIL の行で書き、目で見るしかない部分はスクリーンショットに撮ります。

`JAVA_HOME` が JDK 21 を指しているか `java` が `PATH` にある必要があります。
テクスチャの生成は `runData` より先に実行してください。

## ライセンス

MIT
