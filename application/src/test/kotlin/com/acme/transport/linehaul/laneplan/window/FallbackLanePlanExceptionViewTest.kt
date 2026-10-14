package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackLanePlanExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackLanePlanException("code-957", "label-606")
        val view = toFallbackLanePlanExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackLanePlanExceptionViews(mutableListOf<FallbackLanePlanException>(item, item)).size, "views")
    }
}
