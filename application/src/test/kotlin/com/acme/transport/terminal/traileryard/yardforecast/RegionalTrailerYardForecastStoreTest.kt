package com.acme.transport.terminal.traileryard.yardforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalTrailerYardForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalTrailerYardForecastStore()
        val item = newRegionalTrailerYardForecast("code-401", "title-475")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
