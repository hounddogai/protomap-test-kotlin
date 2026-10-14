package com.acme.transport.linehaul.trailer.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerTierStore()
        val item = newTrailerTier("code-537", "label-806")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
