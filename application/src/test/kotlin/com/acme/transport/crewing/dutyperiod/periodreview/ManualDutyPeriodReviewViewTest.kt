package com.acme.transport.crewing.dutyperiod.periodreview

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDutyPeriodReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDutyPeriodReview("code-272", "title-384")
        val view = toManualDutyPeriodReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDutyPeriodReviewViews(mutableListOf<ManualDutyPeriodReview>(item, item)).size, "views")
    }
}
