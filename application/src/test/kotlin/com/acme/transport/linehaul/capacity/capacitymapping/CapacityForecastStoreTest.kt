package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityForecastStore()
        val item = newCapacityForecast("code-230", "name-82")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
