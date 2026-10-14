package com.acme.transport.network.hub.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubReviewStore()
        val item = newHubReview("code-261", "title-27")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
