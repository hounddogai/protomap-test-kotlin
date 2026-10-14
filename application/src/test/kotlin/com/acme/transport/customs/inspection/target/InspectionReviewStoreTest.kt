package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionReviewStore()
        val item = newInspectionReview("code-216", "name-637")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
