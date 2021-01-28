# Yahoo!ID連携でOAuth2.0 Authorization Code Flowを理解する

Yahoo!ID連携APIはOAuth2.0, OpenIDに準拠しています。
このAPIを叩いてユーザのプロファイル情報を取得・表示する
Webアプリの作成を通じてAuthorization Code Flowを理解してみます

**SpringにはOAuth2.0をサポートするセキュリティ機能が既に用意されています。**  
ここでは勉強の意味で車輪の再発明を承知のうえで実装します。

## 開発環境
- 言語：Kotlin
- フレームワーク：Spring Boot

## 準備 

### Yahoo! ID連携の準備
まずはYahooアカウント（無料ユーザでも可能）を作成。次のページから連携するアプリケーションを追加。種類は「 サーバーサイド（Yahoo! ID連携 v2）」を選択。  
[Yahoo! デベロッパーネットワーク - アプリケーションの管理](https://e.developer.yahoo.co.jp/dashboard/)

今回は数あるフローの中でも Authorization Code Flowを利用する。  
- [OAuth 2.0 Authorization Code フローの詳細](https://developer.yahoo.co.jp/yconnect/v2/authorization_code/)
- [Yahoo! ID 連携における各種エンドポイントの詳細](https://developer.yahoo.co.jp/yconnect/v2/authorization_code/authorization.html)

取得した認証情報は`/src/main/resources/application-credentials.properties`に保存すること。
```:properties
app.yahoo.client_id=${your_client_id}
app.yahoo.secrete=${your_secrete}
```

### 環境構築

kotlinでWebアプリ開発するフレームワーク spring を準備。[spring initializr](https://start.spring.io/) でプロジェクトの雛形を簡単に作成できるので、依存関係として
- Spring Web
- Spring Boot DevTools

を最低限選択して Generate! あとはダウンロードして解凍してからIDEで開く。

**注意**  
今回選択した spring 2.4.2 は Java 11 (version 55)でコンパイルされていたため、ローカルの開発環境のJREバージョン（IDEで設定できるSDKバージョン）
も Java 11 以上にしないとコンパイルは通るが実行できない。  
[参考：Baelbung - How to Fix java.lang.UnsupportedClassVersionError](https://www.baeldung.com/java-lang-unsupportedclassversion)

## 実行

Gradle > bootRun を選んでデバックなり実行すると`http://localhost:8080`にテストサーバが起動する。