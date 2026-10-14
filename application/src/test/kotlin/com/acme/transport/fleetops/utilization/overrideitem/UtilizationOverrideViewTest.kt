package com.acme.transport.fleetops.utilization.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationOverride("code-78", "title-875")
        val view = toUtilizationOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationOverrideViews(mutableListOf<UtilizationOverride>(item, item)).size, "views")
    }
}
