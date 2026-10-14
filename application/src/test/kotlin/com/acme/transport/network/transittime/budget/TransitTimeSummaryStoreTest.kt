package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeSummaryStore()
        val item = newTransitTimeSummary("code-465", "name-907")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
