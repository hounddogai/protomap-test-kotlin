package com.acme.transport.emissions.carbonreport.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportReviewStore()
        val item = newCarbonReportReview("code-58", "label-877")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
