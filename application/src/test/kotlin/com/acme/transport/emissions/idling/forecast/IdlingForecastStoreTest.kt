package com.acme.transport.emissions.idling.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingForecastStore()
        val item = newIdlingForecast("code-593", "name-480")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
