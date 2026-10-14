package com.acme.transport.lastmile.attempt.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptSegmentStore()
        val item = newAttemptSegment("code-8", "name-115")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
