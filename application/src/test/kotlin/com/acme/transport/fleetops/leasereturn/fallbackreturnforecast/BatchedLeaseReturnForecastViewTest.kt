package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedLeaseReturnForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedLeaseReturnForecast("code-858", "title-465")
        val view = toBatchedLeaseReturnForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedLeaseReturnForecastViews(mutableListOf<BatchedLeaseReturnForecast>(item, item)).size, "views")
    }
}
