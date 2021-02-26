package blue.starry.setlist

import blue.starry.penicillin.PenicillinClient
import blue.starry.penicillin.core.session.config.account
import blue.starry.penicillin.core.session.config.application
import blue.starry.penicillin.core.session.config.httpClient
import blue.starry.penicillin.core.session.config.token
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.http.*

val SetlistHttpClient = HttpClient {
    defaultRequest {
        userAgent("setlist (+https://github.com/SlashNephy/setlist)")
    }
}

val SetlistTwitterClient = PenicillinClient {
    account {
        application(Env.TWITTER_CK, Env.TWITTER_CS)
        token(Env.TWITTER_AT, Env.TWITTER_ATS)
    }
    httpClient(SetlistHttpClient)
}
