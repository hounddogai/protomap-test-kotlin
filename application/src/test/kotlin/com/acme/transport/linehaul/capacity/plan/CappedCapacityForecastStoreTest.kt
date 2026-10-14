package com.acme.transport.linehaul.capacity.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedCapacityForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedCapacityForecastStore()
        val item = newCappedCapacityForecast("code-198", "name-649")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
