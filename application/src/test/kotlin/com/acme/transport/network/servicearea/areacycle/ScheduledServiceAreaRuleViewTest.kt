package com.acme.transport.network.servicearea.areacycle

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledServiceAreaRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledServiceAreaRule("code-414", "name-505")
        val view = toScheduledServiceAreaRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledServiceAreaRuleViews(mutableListOf<ScheduledServiceAreaRule>(item, item)).size, "views")
    }
}
