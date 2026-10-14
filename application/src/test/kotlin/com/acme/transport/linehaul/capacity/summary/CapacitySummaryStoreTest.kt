package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacitySummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacitySummaryStore()
        val item = newCapacitySummary("code-943", "name-274")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
