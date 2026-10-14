package com.acme.transport.crewing.restbreak.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakOverrideStore()
        val item = newRestBreakOverride("code-631", "name-125")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
