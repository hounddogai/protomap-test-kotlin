package com.acme.transport.terminal.dockdoor.dooragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorPolicyStore()
        val item = newDockDoorPolicy("code-194", "name-200")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
