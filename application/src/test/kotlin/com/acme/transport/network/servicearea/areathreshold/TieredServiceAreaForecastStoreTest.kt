package com.acme.transport.network.servicearea.areathreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredServiceAreaForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredServiceAreaForecastStore()
        val item = newTieredServiceAreaForecast("code-643", "label-80")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
