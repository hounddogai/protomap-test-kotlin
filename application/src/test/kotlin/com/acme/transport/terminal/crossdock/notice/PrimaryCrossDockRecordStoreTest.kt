package com.acme.transport.terminal.crossdock.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCrossDockRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCrossDockRecordStore()
        val item = newPrimaryCrossDockRecord("code-229", "label-977")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
