package com.acme.transport.crewing.qualification.template

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationTemplate("code-135", "label-126")
        val view = toQualificationTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationTemplateViews(mutableListOf<QualificationTemplate>(item, item)).size, "views")
    }
}
