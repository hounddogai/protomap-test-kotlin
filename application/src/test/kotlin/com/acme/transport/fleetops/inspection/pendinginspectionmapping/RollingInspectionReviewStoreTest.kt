package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingInspectionReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingInspectionReviewStore()
        val item = newRollingInspectionReview("code-732", "name-118")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
