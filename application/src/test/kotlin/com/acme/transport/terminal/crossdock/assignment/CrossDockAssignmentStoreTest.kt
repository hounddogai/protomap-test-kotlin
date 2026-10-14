package com.acme.transport.terminal.crossdock.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockAssignmentStore()
        val item = newCrossDockAssignment("code-95", "name-462")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
