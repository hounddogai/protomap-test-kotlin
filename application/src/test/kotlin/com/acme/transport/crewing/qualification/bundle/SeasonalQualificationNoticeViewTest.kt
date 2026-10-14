package com.acme.transport.crewing.qualification.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalQualificationNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalQualificationNotice("code-743", "label-472")
        val view = toSeasonalQualificationNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalQualificationNoticeViews(mutableListOf<SeasonalQualificationNotice>(item, item)).size, "views")
    }
}
