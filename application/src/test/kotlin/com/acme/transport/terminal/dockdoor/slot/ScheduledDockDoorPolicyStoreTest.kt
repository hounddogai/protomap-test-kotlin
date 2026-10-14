package com.acme.transport.terminal.dockdoor.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDockDoorPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDockDoorPolicyStore()
        val item = newScheduledDockDoorPolicy("code-599", "title-650")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
