package com.acme.transport.linehaul.relay.weightedrelayrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingRelayForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingRelayForecastStore()
        val item = newPendingRelayForecast("code-127", "label-433")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
