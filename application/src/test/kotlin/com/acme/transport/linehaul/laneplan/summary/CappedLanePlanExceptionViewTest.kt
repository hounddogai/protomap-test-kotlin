package com.acme.transport.linehaul.laneplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedLanePlanExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedLanePlanException("code-209", "name-256")
        val view = toCappedLanePlanExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedLanePlanExceptionViews(mutableListOf<CappedLanePlanException>(item, item)).size, "views")
    }
}
