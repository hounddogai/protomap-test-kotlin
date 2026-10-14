package com.acme.transport.network.zone.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalZoneTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalZoneTemplate("code-738", "title-234")
        val view = toGlobalZoneTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalZoneTemplateViews(mutableListOf<GlobalZoneTemplate>(item, item)).size, "views")
    }
}
