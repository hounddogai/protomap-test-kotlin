package com.acme.transport.terminal.traileryard.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultTrailerYardWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultTrailerYardWindowStore()
        val item = newDefaultTrailerYardWindow("code-688", "label-97")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
