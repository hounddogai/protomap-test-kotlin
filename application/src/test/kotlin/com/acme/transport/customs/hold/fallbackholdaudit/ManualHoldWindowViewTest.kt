package com.acme.transport.customs.hold.fallbackholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualHoldWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualHoldWindow("code-13", "name-767")
        val view = toManualHoldWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualHoldWindowViews(mutableListOf<ManualHoldWindow>(item, item)).size, "views")
    }
}
