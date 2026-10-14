package com.acme.transport.linehaul.tender.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalTenderDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalTenderDraft("code-8", "name-238")
        val view = toGlobalTenderDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalTenderDraftViews(mutableListOf<GlobalTenderDraft>(item, item)).size, "views")
    }
}
