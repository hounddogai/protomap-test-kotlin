package com.acme.transport.safety.cameraevent.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCameraEventBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCameraEventBudget("code-174", "title-2")
        val view = toManualCameraEventBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCameraEventBudgetViews(mutableListOf<ManualCameraEventBudget>(item, item)).size, "views")
    }
}
