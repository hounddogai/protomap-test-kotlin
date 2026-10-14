package com.acme.transport.terminal.crossdock.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockEntryStore()
        val item = newCrossDockEntry("code-347", "title-160")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
