package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictClearanceSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictClearanceSegmentStore()
        val item = newStrictClearanceSegment("code-165", "title-524")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
