package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockMappingStore()
        val item = newCrossDockMapping("code-682", "title-690")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
