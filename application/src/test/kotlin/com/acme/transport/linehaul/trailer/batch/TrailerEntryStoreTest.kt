package com.acme.transport.linehaul.trailer.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerEntryStore()
        val item = newTrailerEntry("code-266", "name-585")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
