package com.acme.transport.network.cutoff.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCutoffForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCutoffForecastStore()
        val item = newDefaultCutoffForecast("code-98", "label-565")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
