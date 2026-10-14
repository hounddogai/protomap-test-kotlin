package com.acme.transport.crewing.dutyperiod.periodreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualDutyPeriodReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualDutyPeriodReviewStore()
        val item = newManualDutyPeriodReview("code-272", "title-384")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
