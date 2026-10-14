package com.acme.transport.terminal.dockdoor.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedDockDoorExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedDockDoorExceptionStore()
        val item = newCappedDockDoorException("code-23", "title-442")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
