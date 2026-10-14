package com.acme.transport.terminal.dockdoor.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorBaselineStore()
        val item = newDockDoorBaseline("code-557", "label-957")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
