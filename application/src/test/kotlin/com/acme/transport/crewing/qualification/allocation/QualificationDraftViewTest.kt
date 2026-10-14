package com.acme.transport.crewing.qualification.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationDraft("code-891", "label-493")
        val view = toQualificationDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationDraftViews(mutableListOf<QualificationDraft>(item, item)).size, "views")
    }
}
