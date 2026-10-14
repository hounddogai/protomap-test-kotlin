package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDockDoorGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDockDoorGroupStore()
        val item = newFallbackDockDoorGroup("code-65", "title-386")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
