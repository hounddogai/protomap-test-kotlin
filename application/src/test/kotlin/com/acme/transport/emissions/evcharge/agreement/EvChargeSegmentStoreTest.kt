package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeSegmentStore()
        val item = newEvChargeSegment("code-994", "name-385")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
