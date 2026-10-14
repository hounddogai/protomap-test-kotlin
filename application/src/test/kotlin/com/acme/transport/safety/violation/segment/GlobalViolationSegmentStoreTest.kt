package com.acme.transport.safety.violation.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalViolationSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalViolationSegmentStore()
        val item = newGlobalViolationSegment("code-169", "title-248")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
