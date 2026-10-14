package com.acme.transport.crewing.qualification.qualificationcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalQualificationMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalQualificationMapping("code-407", "title-445")
        val view = toSeasonalQualificationMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalQualificationMappingViews(mutableListOf<SeasonalQualificationMapping>(item, item)).size, "views")
    }
}
