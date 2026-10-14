package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalCameraEventBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalCameraEventBudget("code-388", "label-986")
        val view = toGlobalCameraEventBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalCameraEventBudgetViews(mutableListOf<GlobalCameraEventBudget>(item, item)).size, "views")
    }
}
