package com.acme.transport.linehaul.laneplan.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanException("code-444", "title-372")
        val view = toLanePlanExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanExceptionViews(mutableListOf<LanePlanException>(item, item)).size, "views")
    }
}
