package com.acme.transport.terminal.dockdoor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDockDoorAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDockDoorAssignmentStore()
        val item = newFallbackDockDoorAssignment("code-200", "name-382")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
