package com.acme.transport.terminal.crossdock.strictdockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyCrossDockEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyCrossDockEntryStore()
        val item = newLegacyCrossDockEntry("code-3", "label-566")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
