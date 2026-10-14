package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventBudget("code-985", "label-566")
        val view = toCameraEventBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventBudgetViews(mutableListOf<CameraEventBudget>(item, item)).size, "views")
    }
}
