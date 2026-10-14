package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedUtilizationPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedUtilizationPreference("code-257", "name-911")
        val view = toCappedUtilizationPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedUtilizationPreferenceViews(mutableListOf<CappedUtilizationPreference>(item, item)).size, "views")
    }
}
