package com.acme.transport.lastmile.drop.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropEntryStore()
        val item = newDropEntry("code-905", "title-991")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
