package com.acme.transport.safety.incident.incidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentDraft("code-351", "label-874")
        val view = toIncidentDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentDraftViews(mutableListOf<IncidentDraft>(item, item)).size, "views")
    }
}
