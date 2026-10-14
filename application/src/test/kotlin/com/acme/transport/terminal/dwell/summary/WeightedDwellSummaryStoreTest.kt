package com.acme.transport.terminal.dwell.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedDwellSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedDwellSummaryStore()
        val item = newWeightedDwellSummary("code-696", "label-219")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
