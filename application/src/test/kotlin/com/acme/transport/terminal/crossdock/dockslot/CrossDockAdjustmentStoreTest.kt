package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockAdjustmentStore()
        val item = newCrossDockAdjustment("code-226", "name-720")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
