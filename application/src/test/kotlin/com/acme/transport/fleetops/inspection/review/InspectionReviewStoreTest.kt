package com.acme.transport.fleetops.inspection.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionReviewStore()
        val item = newInspectionReview("code-114", "title-635")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
