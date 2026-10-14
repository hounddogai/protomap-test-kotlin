package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationSlot("code-947", "title-118")
        val view = toQualificationSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationSlotViews(mutableListOf<QualificationSlot>(item, item)).size, "views")
    }
}
