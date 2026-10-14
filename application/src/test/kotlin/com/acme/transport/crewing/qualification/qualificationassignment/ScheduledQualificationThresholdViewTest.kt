package com.acme.transport.crewing.qualification.qualificationassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledQualificationThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledQualificationThreshold("code-426", "name-747")
        val view = toScheduledQualificationThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledQualificationThresholdViews(mutableListOf<ScheduledQualificationThreshold>(item, item)).size, "views")
    }
}
