# Holder CLI アプリケーション

このリポジトリではIssuerのテストを目的として、アクセストークン、VCの発行リクエストおよびレスポンスのパースと保持を行うHolderアプリを実装します。SD-JWT-VC仕様に基づいて設計されたコマンドラインツールで、アクセストークンの取得、クレデンシャルの発行リクエスト、クレデンシャルの保持と提示をサポートします。シンプルなCLIインターフェイスです。Kotlinの勉強の為に書いているので非常に雑な実装です。

---

## 特徴

- **アクセストークンの取得**  
  Issuerからアクセストークンを取得し、後続のリクエストに利用可能。

- **Verifiable Credential (VC) の発行リクエスト**  
  保持するアクセストークンを用いてIssuerにVCをリクエストします。

- **選択的開示の準備**  
  SD-JWT-VC形式のクレデンシャルを保持し、Verifierに対して必要な情報のみを提示する設計に対応可能。

---

## 実装状況

### **1. アクセストークンの取得**
現在実装済み。Issuerからアクセストークンを取得し、CLI内で保持します。

#### 実装の概要
- **エンドポイント**: `POST /token`
- **使用ライブラリ**: `OkHttp` を用いた非同期リクエスト処理。
- **主要メソッド**: `AccessTokenHandler.requestAccessToken`

#### リクエスト例
```json
{
    "vct": "https://fujita-issuer.example.com/vc/mynumber",
    "grant_type": "client_credentials",
    "auth_data": {
        "identificationParam": "dummy",
        "name": "fujita taro",
        "gender": "male",
        "address": "aichi",
        "birthdate": "2018-10-10"
    },
    "scope": "credential_issue"
}
```

#### レスポンス例
```json
{
  "access_token": "eyJhbGciOi...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

---

### **2. Verifiable Credential (VC) の発行リクエスト**
現在実装済み。アクセストークンを用いて、IssuerにVCをリクエストします。

#### 実装の概要
- **エンドポイント**: `POST /credential`
- **使用ライブラリ**: `OkHttp` を用いた非同期リクエスト処理。
- **主要メソッド**: `CredentialHandler.requestVerifiableCredential`

#### リクエスト例
```json
{
  "format": "sd_jwt_vc",
  "types": ["VerifiableCredential", "https://fujita-issuer.example.com/vc/mynumber"],
  "cnf": {
    "jwk": {
      "alg": "EdDSA",
      "crv": "Ed25519",
      "kty": "OKP",
      "use": "sig",
      "x": "-w76fv0jlTZo3H6mtdcJrJZfJ4Ltm2MJi09V_zxM3Vo"
    }
  },
  "proof": {
    "proof_type": "jwt",
    "jwt": "eyJ0eXAiOiJvcGVuaWQ0dmNpLXByb29mK2p3dCIsImFsZyI6..."
  }
}
```

#### レスポンス例
```json
{
    "format": "sd_jwt_vc",
    "credential": "eyJhbGciOiJFUzI1NiIsInR5cCI6ImRjK3NkLWp3dCJ9...",
    "c_nonce": "f021cd55-553f-4344-93aa-80a1be9191db",
    "c_nonce_expires_in": 300
}
```

---

## 使用方法

### **アプリの起動**
1. プロジェクトをクローンし、ビルドします。
   ```bash
   ./gradlew build
   ```
2. アプリを起動します。
   ```bash
   ./gradlew run
   ```

### **CLI操作**
アプリ起動後、CLIメニューから以下の操作が可能です：

```
Holder CLI Application
1. Request Access Token
2. Request Verifiable Credential
0. Exit
Select an option: 
```

1. **アクセストークンの取得**
   - オプション番号 `1` を選択し、Issuerからアクセストークンを取得します。

2. **VCの発行リクエスト**
   - オプション番号 `2` を選択し、保持したアクセストークンを用いてVCをリクエストします。

3. **終了**
   - オプション番号 `0` を選択してアプリケーションを終了します。

---

## 今後の設計

1. **VP (Verifiable Presentation) の生成**
   - SD-JWT-VCをもとに、選択的開示をサポートするVPを生成する機能を追加予定。
   - Verifierの要求に基づいて柔軟なクレームの開示をサポート。

2. **UIの改善**
   - CLIの操作性を向上させ、ユーザーフィードバックを増強。
   - そのうちAndroid StudioでUI実装します。

3. **テストスイートの整備**
   - IssuerやVerifierとの統合テストの追加。

4. **非同期処理の改良**
   - 現在のスリープベースの簡易処理を非同期メカニズムに置き換え。

---