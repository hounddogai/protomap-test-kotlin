package com.acme.transport.terminal.dwell.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDwellAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDwellAssignmentStore()
        val item = newPendingDwellAssignment("code-850", "title-533")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
