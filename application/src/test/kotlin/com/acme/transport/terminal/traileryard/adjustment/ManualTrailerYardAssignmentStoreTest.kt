package com.acme.transport.terminal.traileryard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTrailerYardAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTrailerYardAssignmentStore()
        val item = newManualTrailerYardAssignment("code-982", "title-436")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
