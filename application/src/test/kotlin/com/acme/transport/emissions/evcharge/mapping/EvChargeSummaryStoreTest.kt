package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeSummaryStore()
        val item = newEvChargeSummary("code-288", "title-497")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
