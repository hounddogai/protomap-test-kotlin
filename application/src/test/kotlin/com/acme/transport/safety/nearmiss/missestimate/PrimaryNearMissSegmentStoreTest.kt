package com.acme.transport.safety.nearmiss.missestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryNearMissSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryNearMissSegmentStore()
        val item = newPrimaryNearMissSegment("code-870", "title-413")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
