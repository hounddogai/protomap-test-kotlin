package com.acme.transport.terminal.dockdoor.dooragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorAssignmentStore()
        val item = newDockDoorAssignment("code-312", "name-469")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
