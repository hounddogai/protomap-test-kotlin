package com.acme.transport.linehaul.capacity.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalCapacityEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalCapacityEstimateStore()
        val item = newSeasonalCapacityEstimate("code-492", "name-428")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
