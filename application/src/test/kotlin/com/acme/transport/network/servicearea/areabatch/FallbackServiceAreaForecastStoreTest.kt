package com.acme.transport.network.servicearea.areabatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackServiceAreaForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackServiceAreaForecastStore()
        val item = newFallbackServiceAreaForecast("code-408", "title-678")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
