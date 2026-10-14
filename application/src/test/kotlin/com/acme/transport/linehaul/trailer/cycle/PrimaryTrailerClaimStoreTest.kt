package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryTrailerClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryTrailerClaimStore()
        val item = newPrimaryTrailerClaim("code-961", "label-888")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
