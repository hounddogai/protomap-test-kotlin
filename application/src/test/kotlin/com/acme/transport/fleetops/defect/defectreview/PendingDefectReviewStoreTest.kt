package com.acme.transport.fleetops.defect.defectreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDefectReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDefectReviewStore()
        val item = newPendingDefectReview("code-671", "name-462")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
