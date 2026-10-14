package com.acme.transport.safety.nearmiss.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissSegmentStore()
        val item = newNearMissSegment("code-101", "label-889")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
