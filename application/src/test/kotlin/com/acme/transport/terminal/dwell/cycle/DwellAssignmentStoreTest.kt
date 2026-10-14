package com.acme.transport.terminal.dwell.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellAssignmentStore()
        val item = newDwellAssignment("code-828", "label-263")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
