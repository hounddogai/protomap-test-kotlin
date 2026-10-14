package com.acme.transport.fleetops.utilization.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultUtilizationPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultUtilizationPreference("code-445", "name-965")
        val view = toDefaultUtilizationPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultUtilizationPreferenceViews(mutableListOf<DefaultUtilizationPreference>(item, item)).size, "views")
    }
}
