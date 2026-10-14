package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalUtilizationEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalUtilizationEstimate("code-277", "title-773")
        assertEquals(GlobalUtilizationEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalUtilizationEstimate("code-277", "title-773")
        try {
            item.moveTo(GlobalUtilizationEstimateStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalUtilizationEstimateStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalUtilizationEstimate("code-277", "title-773")
        item.status = GlobalUtilizationEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalUtilizationEstimateStatus.PROPOSED) }
    }
}
