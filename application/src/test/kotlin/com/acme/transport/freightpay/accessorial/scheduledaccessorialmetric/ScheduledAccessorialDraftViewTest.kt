package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledAccessorialDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledAccessorialDraft("code-562", "name-435")
        val view = toScheduledAccessorialDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledAccessorialDraftViews(mutableListOf<ScheduledAccessorialDraft>(item, item)).size, "views")
    }
}
