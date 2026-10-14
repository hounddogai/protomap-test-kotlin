package com.acme.transport.safety.incident.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryIncidentQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryIncidentQuota("code-608", "name-904")
        val view = toPrimaryIncidentQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryIncidentQuotaViews(mutableListOf<PrimaryIncidentQuota>(item, item)).size, "views")
    }
}
