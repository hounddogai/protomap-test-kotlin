package com.acme.transport.terminal.dockdoor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualDockDoorAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualDockDoorAssignmentStore()
        val item = newManualDockDoorAssignment("code-594", "title-356")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
