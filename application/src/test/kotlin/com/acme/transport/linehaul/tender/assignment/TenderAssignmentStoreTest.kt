package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderAssignmentStore()
        val item = newTenderAssignment("code-966", "label-959")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
