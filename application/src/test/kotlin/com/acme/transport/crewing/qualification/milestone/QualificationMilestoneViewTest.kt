package com.acme.transport.crewing.qualification.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationMilestone("code-564", "name-235")
        val view = toQualificationMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationMilestoneViews(mutableListOf<QualificationMilestone>(item, item)).size, "views")
    }
}
