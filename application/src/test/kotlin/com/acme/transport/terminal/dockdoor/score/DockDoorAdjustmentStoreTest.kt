package com.acme.transport.terminal.dockdoor.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorAdjustmentStore()
        val item = newDockDoorAdjustment("code-389", "title-478")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
