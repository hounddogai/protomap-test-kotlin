package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictEvChargeSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictEvChargeSummaryStore()
        val item = newStrictEvChargeSummary("code-47", "label-781")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
