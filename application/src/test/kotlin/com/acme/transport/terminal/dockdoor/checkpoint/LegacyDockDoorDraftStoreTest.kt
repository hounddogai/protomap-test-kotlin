package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyDockDoorDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyDockDoorDraftStore()
        val item = newLegacyDockDoorDraft("code-444", "title-460")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
