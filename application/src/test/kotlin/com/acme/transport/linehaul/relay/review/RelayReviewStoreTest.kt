package com.acme.transport.linehaul.relay.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayReviewStore()
        val item = newRelayReview("code-879", "name-520")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
