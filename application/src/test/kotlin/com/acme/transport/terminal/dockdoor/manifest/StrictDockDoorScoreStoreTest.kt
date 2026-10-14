package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDockDoorScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDockDoorScoreStore()
        val item = newStrictDockDoorScore("code-724", "label-92")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
