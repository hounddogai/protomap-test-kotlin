package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedQualificationMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedQualificationMilestone("code-623", "title-367")
        val view = toBatchedQualificationMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedQualificationMilestoneViews(mutableListOf<BatchedQualificationMilestone>(item, item)).size, "views")
    }
}
