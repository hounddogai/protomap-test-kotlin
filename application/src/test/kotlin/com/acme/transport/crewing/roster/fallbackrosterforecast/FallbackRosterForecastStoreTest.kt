package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackRosterForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackRosterForecastStore()
        val item = newFallbackRosterForecast("code-323", "title-810")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
