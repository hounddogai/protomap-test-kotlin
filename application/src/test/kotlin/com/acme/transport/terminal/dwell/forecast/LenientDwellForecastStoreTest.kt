package com.acme.transport.terminal.dwell.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDwellForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDwellForecastStore()
        val item = newLenientDwellForecast("code-628", "name-503")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
