package com.acme.transport.lastmile.drop.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropRecordStore()
        val item = newDropRecord("code-432", "label-916")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
