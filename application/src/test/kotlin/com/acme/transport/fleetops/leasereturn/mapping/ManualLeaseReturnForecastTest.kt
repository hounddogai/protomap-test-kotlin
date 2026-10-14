package com.acme.transport.fleetops.leasereturn.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualLeaseReturnForecastTest {
    @Test
    fun `starts in created`() {
        val item = newManualLeaseReturnForecast("code-467", "label-792")
        assertEquals(ManualLeaseReturnForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newManualLeaseReturnForecast("code-467", "label-792")
        try {
            item.moveTo(ManualLeaseReturnForecastStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualLeaseReturnForecastStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualLeaseReturnForecast("code-467", "label-792")
        item.status = ManualLeaseReturnForecastStatus.RETIRED
        assertFails("move") { item.moveTo(ManualLeaseReturnForecastStatus.CREATED) }
    }
}
