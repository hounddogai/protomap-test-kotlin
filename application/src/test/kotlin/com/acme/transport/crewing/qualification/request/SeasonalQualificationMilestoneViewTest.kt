package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalQualificationMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalQualificationMilestone("code-705", "label-971")
        val view = toSeasonalQualificationMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalQualificationMilestoneViews(mutableListOf<SeasonalQualificationMilestone>(item, item)).size, "views")
    }
}
