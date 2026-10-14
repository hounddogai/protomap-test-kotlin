package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayForecastStore()
        val item = newRelayForecast("code-948", "label-618")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
