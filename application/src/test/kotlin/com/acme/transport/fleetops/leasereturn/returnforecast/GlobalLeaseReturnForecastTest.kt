package com.acme.transport.fleetops.leasereturn.returnforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalLeaseReturnForecastTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalLeaseReturnForecast("code-229", "label-2")
        assertEquals(GlobalLeaseReturnForecastStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newGlobalLeaseReturnForecast("code-229", "label-2")
        try {
            item.moveTo(GlobalLeaseReturnForecastStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalLeaseReturnForecastStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalLeaseReturnForecast("code-229", "label-2")
        item.status = GlobalLeaseReturnForecastStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalLeaseReturnForecastStatus.DRAFT) }
    }
}
