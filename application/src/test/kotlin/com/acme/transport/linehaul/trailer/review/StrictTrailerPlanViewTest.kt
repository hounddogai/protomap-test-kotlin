package com.acme.transport.linehaul.trailer.review

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTrailerPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTrailerPlan("code-8", "label-865")
        val view = toStrictTrailerPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTrailerPlanViews(mutableListOf<StrictTrailerPlan>(item, item)).size, "views")
    }
}
