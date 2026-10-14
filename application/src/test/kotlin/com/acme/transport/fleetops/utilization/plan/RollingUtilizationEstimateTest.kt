package com.acme.transport.fleetops.utilization.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingUtilizationEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingUtilizationEstimate("code-559", "name-188")
        assertEquals(RollingUtilizationEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRollingUtilizationEstimate("code-559", "name-188")
        try {
            item.moveTo(RollingUtilizationEstimateStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingUtilizationEstimateStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingUtilizationEstimate("code-559", "name-188")
        item.status = RollingUtilizationEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(RollingUtilizationEstimateStatus.IDLE) }
    }
}
