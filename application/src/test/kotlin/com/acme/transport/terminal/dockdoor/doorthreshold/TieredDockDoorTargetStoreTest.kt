package com.acme.transport.terminal.dockdoor.doorthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredDockDoorTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredDockDoorTargetStore()
        val item = newTieredDockDoorTarget("code-384", "label-582")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
