package com.acme.transport.customs.clearance.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackClearanceAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackClearanceAssignmentStore()
        val item = newFallbackClearanceAssignment("code-746", "label-666")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
