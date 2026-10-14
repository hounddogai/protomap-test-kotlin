package com.acme.transport.terminal.dockdoor.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDockDoorTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDockDoorTierStore()
        val item = newPendingDockDoorTier("code-553", "label-903")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
