package com.acme.transport.lastmile.redelivery.redeliverytemplate

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledRedeliveryTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledRedeliveryTemplate("code-812", "name-820")
        val view = toScheduledRedeliveryTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledRedeliveryTemplateViews(mutableListOf<ScheduledRedeliveryTemplate>(item, item)).size, "views")
    }
}
