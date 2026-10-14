package com.acme.transport.linehaul.relay.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveRelayReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveRelayReviewStore()
        val item = newAdaptiveRelayReview("code-535", "label-31")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
