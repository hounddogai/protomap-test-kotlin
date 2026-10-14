package com.acme.transport.terminal.dockdoor.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryDockDoorSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryDockDoorSummaryStore()
        val item = newPrimaryDockDoorSummary("code-31", "title-795")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
