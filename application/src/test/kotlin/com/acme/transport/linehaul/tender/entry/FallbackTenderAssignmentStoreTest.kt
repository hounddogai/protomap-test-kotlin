package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackTenderAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackTenderAssignmentStore()
        val item = newFallbackTenderAssignment("code-741", "label-292")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
