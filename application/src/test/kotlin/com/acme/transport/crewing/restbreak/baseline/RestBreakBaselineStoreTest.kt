package com.acme.transport.crewing.restbreak.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakBaselineStore()
        val item = newRestBreakBaseline("code-636", "name-173")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
