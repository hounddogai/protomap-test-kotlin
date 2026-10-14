package com.acme.transport.terminal.crossdock.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockProfileStore()
        val item = newCrossDockProfile("code-685", "title-823")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
