package com.acme.transport.terminal.traileryard.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardProfileStore()
        val item = newTrailerYardProfile("code-410", "label-572")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
