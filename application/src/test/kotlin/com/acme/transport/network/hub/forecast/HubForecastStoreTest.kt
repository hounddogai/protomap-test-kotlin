package com.acme.transport.network.hub.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubForecastStore()
        val item = newHubForecast("code-547", "name-960")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
