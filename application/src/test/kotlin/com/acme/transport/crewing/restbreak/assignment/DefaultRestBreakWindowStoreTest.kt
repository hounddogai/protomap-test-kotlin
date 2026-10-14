package com.acme.transport.crewing.restbreak.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultRestBreakWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultRestBreakWindowStore()
        val item = newDefaultRestBreakWindow("code-250", "title-27")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
