package com.acme.transport.network.transittime.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledTransitTimeSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledTransitTimeSummaryStore()
        val item = newScheduledTransitTimeSummary("code-453", "name-292")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
