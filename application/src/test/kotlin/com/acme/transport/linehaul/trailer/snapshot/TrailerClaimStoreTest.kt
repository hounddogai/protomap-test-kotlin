package com.acme.transport.linehaul.trailer.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerClaimStore()
        val item = newTrailerClaim("code-822", "label-506")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
