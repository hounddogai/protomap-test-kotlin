package com.acme.transport.terminal.dockdoor.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDockDoorExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDockDoorExceptionStore()
        val item = newFallbackDockDoorException("code-680", "name-354")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
