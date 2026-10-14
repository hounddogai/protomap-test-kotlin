package com.acme.transport.customs.inspection.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyInspectionReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyInspectionReviewStore()
        val item = newLegacyInspectionReview("code-216", "title-126")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
