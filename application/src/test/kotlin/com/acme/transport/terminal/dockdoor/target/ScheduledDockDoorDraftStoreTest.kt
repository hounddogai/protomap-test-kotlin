package com.acme.transport.terminal.dockdoor.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDockDoorDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDockDoorDraftStore()
        val item = newScheduledDockDoorDraft("code-357", "title-911")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
