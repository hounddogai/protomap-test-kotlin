package com.acme.transport.linehaul.laneplan.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveLanePlanExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveLanePlanException("code-206", "title-636")
        val view = toAdaptiveLanePlanExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveLanePlanExceptionViews(mutableListOf<AdaptiveLanePlanException>(item, item)).size, "views")
    }
}
