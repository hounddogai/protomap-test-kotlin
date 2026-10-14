package com.acme.transport.terminal.dockdoor.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDockDoorNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDockDoorNoticeStore()
        val item = newScheduledDockDoorNotice("code-764", "title-872")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
