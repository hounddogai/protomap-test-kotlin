package com.acme.transport.fleetops.leasereturn.returnforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalLeaseReturnForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalLeaseReturnForecast("code-229", "label-2")
        val view = toGlobalLeaseReturnForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalLeaseReturnForecastViews(mutableListOf<GlobalLeaseReturnForecast>(item, item)).size, "views")
    }
}
