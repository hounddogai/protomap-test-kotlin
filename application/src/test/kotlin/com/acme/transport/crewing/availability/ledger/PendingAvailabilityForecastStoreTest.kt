package com.acme.transport.crewing.availability.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingAvailabilityForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingAvailabilityForecastStore()
        val item = newPendingAvailabilityForecast("code-194", "name-533")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
