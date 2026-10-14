package com.acme.transport.lastmile.redelivery.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingRedeliveryTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingRedeliveryTemplate("code-505", "label-743")
        val view = toRollingRedeliveryTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingRedeliveryTemplateViews(mutableListOf<RollingRedeliveryTemplate>(item, item)).size, "views")
    }
}
