# SAmusic 🎧

App Android de streaming musical — catálogo real, player completo, playlists e favoritos.
Marca, nome e identidade visual **próprios** (violeta/âmbar, deliberadamente diferente do Spotify).

Construído em Kotlin + Jetpack Compose + Media3/ExoPlayer, com catálogo **Jamendo** (Creative
Commons) como fonte de áudio real e tocável.

> **Referências de UX:** Spotify (navegação/player) e HiMelody (player offline Android: fila,
> favoritos, histórico, download). Do HiMelody foram aproveitadas apenas ideias de funcionalidade
> **legítimas** — nada de raspar áudio de fontes não autorizadas, que é o mecanismo de fundo desse
> tipo de app. Aqui o download offline usa a URL de download que o próprio Jamendo fornece sob
> licença Creative Commons (campo `audiodownload`), quando a faixa o permite.

> **Por que não o Spotify?** A Web API do Spotify entrega apenas metadados e previews de 30 s,
> nunca o áudio completo, e seu ToS proíbe redistribuir/baixar faixas. Tocar o catálogo completo
> deles exigiria o SDK oficial (conta Premium do próprio usuário) ou burlar o DRM — o que este
> projeto **não** faz. O Spotify serviu apenas de referência de UX. A fonte de áudio é o Jamendo,
> que disponibiliza milhares de faixas legalmente tocáveis via API gratuita.

---

## 1. O que está funcionando

| Área | Status |
|---|---|
| Catálogo real (Jamendo), paginado/infinite scroll | ✅ implementado |
| Busca por nome (músicas/artistas) | ✅ |
| Exploração por gênero (chips) | ✅ |
| Player Media3/ExoPlayer: play/pause, próxima/anterior, seek | ✅ |
| Shuffle, repeat (off/all/one), fila de reprodução | ✅ |
| Reprodução em segundo plano + notificação + tela de bloqueio | ✅ (via MediaSessionService) |
| Pausar ao desconectar fone / foco de áudio | ✅ |
| Favoritos (Room), persistente | ✅ |
| Playlists: criar, excluir (base para add/remover/reordenar) | ✅ criar/excluir; add/remover prontos no repositório |
| Histórico de reprodução | ✅ |
| Mini player + player em tela cheia | ✅ |
| Tema escuro (identidade própria) | ✅ |
| Estados vazios e de carregamento | ✅ |

## 2. O que foi testado

- Teste unitário JVM do mapeamento `TrackDto → Track` e formatação de duração
  (`app/src/test/java/com/samusic/app/TrackMappingTest.kt`).
- **Importante / honestidade:** este projeto foi escrito e revisado, mas **não foi compilado nem
  testado no ambiente onde foi gerado** (sem Android SDK e sem acesso de rede ao Gradle/AGP ali).
  Rode `./gradlew test` e `./gradlew assembleDebug` na sua máquina para compilar, executar os
  testes e gerar o APK. Pequenos ajustes de versão podem ser necessários conforme seu ambiente.

## 3. Qual fonte musical está integrada

**Jamendo API v3.0** — catálogo Creative Commons com áudio completo e streamável.
Gratuita, exige apenas um `client_id`. Importação de arquivos locais está prevista na arquitetura
(permissão `READ_MEDIA_AUDIO` já declarada) como fonte complementar.

## 4. Limitações conhecidas

- O catálogo é o do Jamendo (música independente/CC), não o acervo comercial do Spotify.
- Downloads offline não vêm habilitados por padrão (respeita licença da fonte); a base de Room
  permite adicionar cache quando a licença permitir.
- Add/remover/reordenar faixas dentro de uma playlist estão implementados no `MusicRepository`,
  mas a tela de detalhe da playlist (UI) é o próximo passo.
- Login/conta: a arquitetura é local-first (sem backend); sincronização entre dispositivos exige
  adicionar um backend depois (ver prompt, seções 11 e 15).

---

## 5. Como configurar e compilar

### Pré-requisitos
- Android Studio (Koala 2024.1+), JDK 17.
- Um `client_id` gratuito do Jamendo: https://devportal.jamendo.com/ → crie uma aplicação.

### Passos
1. Copie `local.properties.example` para `local.properties` (se o Android Studio ainda não criou um).
2. Adicione sua chave:
   ```properties
   JAMENDO_CLIENT_ID=suachaveaqui
   ```
   (O `sdk.dir` o Android Studio preenche sozinho.)
3. Abra a pasta no Android Studio e deixe o Gradle sincronizar (baixa Gradle 8.9 + dependências).
4. Rode em um emulador ou dispositivo.

Sem a chave, o app abre normalmente e mostra um aviso de "Configuração necessária" nas telas de
catálogo — nada é simulado silenciosamente.

## 6. Gerar o APK

```bash
# APK de depuração (para testes)
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk

# APK de release (precisa de keystore de assinatura; veja abaixo)
./gradlew assembleRelease
```

Rodar os testes:
```bash
./gradlew test
```

### Instalar o APK no celular
1. Transfira `app-debug.apk` para o aparelho.
2. Ative "Instalar apps de fontes desconhecidas" para o seu gerenciador de arquivos.
3. Abra o APK e instale. (Ou via `adb install app-debug.apk`.)

### Assinatura de release (para Google Play, futuro)
Gere um keystore e configure um `signingConfig` no `app/build.gradle.kts`:
```bash
keytool -genkey -v -keystore samusic.jks -keyalg RSA -keysize 2048 -validity 10000 -alias samusic
```

## 7. Configurações adicionais ainda necessárias

- `JAMENDO_CLIENT_ID` em `local.properties` (obrigatório para o catálogo).
- (Opcional) Keystore de release para publicar.
- (Opcional/futuro) Backend para contas, sincronização e recursos sociais.

---

## Arquitetura

```
ui/            Compose: navegação, Home, Search, Library, Player, componentes, tema
  ViewModels   Home / Search / Library + factory (DI manual)
playback/      MusicService (MediaSessionService) + PlaybackController (MediaController)
data/
  remote/      Retrofit + Moshi → Jamendo API (tracks, search, byTag)
  local/       Room: favoritos, playlists, histórico
  repo/        MusicRepository: une rede + persistência, expõe Flow/Outcome
```

- **Kotlin + Jetpack Compose + Material 3**
- **Media3/ExoPlayer** para reprodução e background
- **Room** (persistência) + **DataStore** (preferências, previsto)
- **Retrofit + Moshi + OkHttp**, **Coil** (imagens), **Coroutines/Flow**
- DI manual leve (sem Hilt) para reduzir processadores de anotação.

## Licenças e conteúdo

O áudio vem do Jamendo sob licenças Creative Commons — respeite os termos de cada faixa.
Veja também `THIRD_PARTY.md` para as bibliotecas usadas.
