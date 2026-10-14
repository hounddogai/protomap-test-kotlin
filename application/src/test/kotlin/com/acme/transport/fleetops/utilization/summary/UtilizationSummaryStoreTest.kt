package com.acme.transport.fleetops.utilization.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationSummaryStore()
        val item = newUtilizationSummary("code-601", "label-287")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
