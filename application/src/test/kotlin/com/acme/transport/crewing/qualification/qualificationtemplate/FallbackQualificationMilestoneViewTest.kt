package com.acme.transport.crewing.qualification.qualificationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackQualificationMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackQualificationMilestone("code-66", "title-183")
        val view = toFallbackQualificationMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackQualificationMilestoneViews(mutableListOf<FallbackQualificationMilestone>(item, item)).size, "views")
    }
}
