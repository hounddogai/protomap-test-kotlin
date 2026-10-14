package com.acme.transport.customs.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedInspectionTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedInspectionTemplate("code-443", "name-651")
        val view = toBatchedInspectionTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedInspectionTemplateViews(mutableListOf<BatchedInspectionTemplate>(item, item)).size, "views")
    }
}
