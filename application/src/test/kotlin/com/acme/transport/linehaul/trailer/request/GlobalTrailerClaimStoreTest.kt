package com.acme.transport.linehaul.trailer.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTrailerClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTrailerClaimStore()
        val item = newGlobalTrailerClaim("code-115", "title-424")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
