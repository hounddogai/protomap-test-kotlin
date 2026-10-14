package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedDockDoorBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedDockDoorBaselineStore()
        val item = newCappedDockDoorBaseline("code-206", "title-515")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
