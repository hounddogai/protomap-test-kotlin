package com.acme.transport.crewing.qualification.qualificationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledQualificationTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledQualificationTemplate("code-289", "label-947")
        val view = toScheduledQualificationTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledQualificationTemplateViews(mutableListOf<ScheduledQualificationTemplate>(item, item)).size, "views")
    }
}
