package com.codequest.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseConfig {
    const val URL = "https://mkrevmnphqlwlnizanjt.supabase.co"
    const val KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1rcmV2bW5waHFsd2xuaXphbmp0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE1MDQwMjUsImV4cCI6MjA4NzA4MDAyNX0.KbQDpqT5yw9PdPYRe1dgk5g1YqNYnjDw7jNZ2BP7lAQ"
}

val supabaseClient: SupabaseClient = createSupabaseClient(
    supabaseUrl = SupabaseConfig.URL,
    supabaseKey = SupabaseConfig.KEY
) {
    defaultSerializer = KotlinXSerializer(Json { ignoreUnknownKeys = true })
    install(Auth)
    install(Postgrest)
    install(Realtime)
    install(Storage)
}
