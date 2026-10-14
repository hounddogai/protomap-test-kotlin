package com.acme.transport.terminal.traileryard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardOverrideStore()
        val item = newTrailerYardOverride("code-754", "name-948")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
