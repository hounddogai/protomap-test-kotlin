package com.acme.transport.terminal.crossdock.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockWindowStore()
        val item = newCrossDockWindow("code-357", "label-132")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
