package com.acme.transport.safety.coaching.coachingsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCoachingSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCoachingSegmentStore()
        val item = newGlobalCoachingSegment("code-871", "name-947")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
