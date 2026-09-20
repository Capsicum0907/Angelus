# Angelus

[English](README.md) | 日本語

空中に設置できるブロックを1つ追加します。目線の先に何もない状態で右クリックすると目の前にブロックが現れます。
即座に壊せてインベントリに入ります。

- 設置できる距離は普通のブロックと同じです。
- 水・背の高い草・雪の中にも置けます。
- 羽根4枚と棒4本で1つ作れます。
- 設定項目はありません。

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
```

`JAVA_HOME` が JDK 21 を指しているか `java` が `PATH` にある必要があります。
テクスチャの生成は `runData` より先に実行してください。

## ライセンス

MIT。
