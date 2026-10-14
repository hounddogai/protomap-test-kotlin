package com.acme.transport.emissions.evcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryEvChargeSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryEvChargeSegmentStore()
        val item = newPrimaryEvChargeSegment("code-646", "label-24")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
