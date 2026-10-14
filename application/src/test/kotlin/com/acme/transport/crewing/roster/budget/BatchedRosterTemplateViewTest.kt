package com.acme.transport.crewing.roster.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRosterTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRosterTemplate("code-69", "name-840")
        val view = toBatchedRosterTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRosterTemplateViews(mutableListOf<BatchedRosterTemplate>(item, item)).size, "views")
    }
}
