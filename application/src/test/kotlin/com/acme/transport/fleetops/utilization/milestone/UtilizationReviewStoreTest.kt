package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationReviewStore()
        val item = newUtilizationReview("code-2", "name-65")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
