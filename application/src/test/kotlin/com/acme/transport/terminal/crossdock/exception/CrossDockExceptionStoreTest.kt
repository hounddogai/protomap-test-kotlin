package com.acme.transport.terminal.crossdock.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockExceptionStore()
        val item = newCrossDockException("code-334", "name-582")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
