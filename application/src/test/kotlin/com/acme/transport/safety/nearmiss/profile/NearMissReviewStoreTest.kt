package com.acme.transport.safety.nearmiss.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissReviewStore()
        val item = newNearMissReview("code-935", "name-141")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
