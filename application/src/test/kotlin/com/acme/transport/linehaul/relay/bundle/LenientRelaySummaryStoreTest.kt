package com.acme.transport.linehaul.relay.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientRelaySummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientRelaySummaryStore()
        val item = newLenientRelaySummary("code-110", "title-526")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
