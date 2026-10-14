package com.acme.transport.fleetops.defect.defectagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectTemplate("code-236", "name-707")
        val view = toDefectTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectTemplateViews(mutableListOf<DefectTemplate>(item, item)).size, "views")
    }
}
