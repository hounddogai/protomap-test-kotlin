package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyCrossDockExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyCrossDockExceptionStore()
        val item = newLegacyCrossDockException("code-268", "title-239")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
