package com.acme.transport.terminal.crossdock.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledCrossDockRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledCrossDockRecordStore()
        val item = newScheduledCrossDockRecord("code-943", "title-260")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
