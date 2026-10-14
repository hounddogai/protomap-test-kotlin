package com.acme.transport.customs.clearance.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceSegmentStore()
        val item = newClearanceSegment("code-974", "title-584")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
