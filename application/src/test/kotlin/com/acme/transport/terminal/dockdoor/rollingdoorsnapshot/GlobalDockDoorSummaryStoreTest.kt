package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalDockDoorSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalDockDoorSummaryStore()
        val item = newGlobalDockDoorSummary("code-697", "label-541")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
