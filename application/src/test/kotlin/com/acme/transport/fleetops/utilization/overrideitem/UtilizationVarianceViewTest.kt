package com.acme.transport.fleetops.utilization.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationVariance("code-776", "name-216")
        val view = toUtilizationVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationVarianceViews(mutableListOf<UtilizationVariance>(item, item)).size, "views")
    }
}
