package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictUtilizationReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictUtilizationReviewStore()
        val item = newStrictUtilizationReview("code-498", "title-236")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
