package com.acme.transport.linehaul.relay.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedRelayReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedRelayReviewStore()
        val item = newCappedRelayReview("code-273", "name-408")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
