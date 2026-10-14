package com.acme.transport.crewing.qualification.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedQualificationTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedQualificationTemplate("code-427", "title-910")
        val view = toBatchedQualificationTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedQualificationTemplateViews(mutableListOf<BatchedQualificationTemplate>(item, item)).size, "views")
    }
}
