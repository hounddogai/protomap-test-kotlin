package com.acme.transport.fleetops.defect.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectReviewStore()
        val item = newDefectReview("code-706", "name-60")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
