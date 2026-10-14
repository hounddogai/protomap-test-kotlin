package com.acme.transport.linehaul.trailer.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerRollupStore()
        val item = newTrailerRollup("code-286", "name-523")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
