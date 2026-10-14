package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationEntry("code-730", "title-875")
        val view = toUtilizationEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationEntryViews(mutableListOf<UtilizationEntry>(item, item)).size, "views")
    }
}
