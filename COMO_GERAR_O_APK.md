# Como obter o APK do SAmusic

O `.zip` é o **código-fonte**. O APK (arquivo instalável) é gerado a partir dele.
Há dois caminhos. O **Caminho A não exige instalar nada no seu PC**.

---

## Caminho A — Build na nuvem pelo GitHub (recomendado, sem instalar nada)

1. Crie uma conta em https://github.com (grátis), se ainda não tiver.
2. Crie um repositório novo (pode ser **privado**). Ex.: `samusic`.
3. Suba os arquivos do projeto:
   - Pelo site: botão **Add file > Upload files** e arraste o conteúdo da pasta `SAmusic`
     (não a pasta em si — o conteúdo: `app/`, `gradle/`, `gradlew`, `settings.gradle.kts`, etc.),
     incluindo a pasta oculta `.github`.
   - Ou pelo Git, se souber usar: `git init`, `git add .`, `git commit`, `git push`.
4. (Opcional, para o catálogo funcionar) Adicione sua chave do Jamendo como segredo:
   - Pegue um `client_id` grátis em https://devportal.jamendo.com/
   - No repositório: **Settings > Secrets and variables > Actions > New repository secret**
   - Nome: `JAMENDO_CLIENT_ID` — Valor: sua chave.
5. O build dispara sozinho ao subir o código. Acompanhe na aba **Actions**.
6. Quando ficar verde ✅, abra o run e baixe o APK em **Artifacts > SAmusic-debug-apk**.
7. Passe o `app-debug.apk` para o celular e instale (ative "instalar de fontes desconhecidas").

> Se o build falhar ❌, abra o run, copie a mensagem de erro e me mande — eu corrijo.
> É normal aparecer algum ajuste na primeira compilação, porque o projeto ainda não foi
> compilado nenhuma vez.

---

## Caminho B — Android Studio no seu PC

1. Instale o Android Studio (grátis; já vem com o Android SDK).
2. Abra a pasta `SAmusic`. Deixe o Gradle sincronizar.
3. Crie `local.properties` (copie de `local.properties.example`) e coloque:
   `JAMENDO_CLIENT_ID=suachave`
4. Menu **Build > Build Bundle(s)/APK(s) > Build APK(s)**, ou no terminal:
   `./gradlew assembleDebug`
5. O APK sai em `app/build/outputs/apk/debug/app-debug.apk`.

---

## Importante (honestidade)

Este projeto foi **escrito e revisado com cuidado, mas ainda não foi compilado**. A primeira
compilação (pelo Caminho A ou B) é o que vai confirmar que está tudo certo e, se houver algum
erro, mostrá-lo. Me mande o log de erro e eu ajusto até o APK sair.
