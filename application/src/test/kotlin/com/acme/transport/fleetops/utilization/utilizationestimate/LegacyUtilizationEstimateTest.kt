package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyUtilizationEstimateTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyUtilizationEstimate("code-5", "title-483")
        assertEquals(LegacyUtilizationEstimateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLegacyUtilizationEstimate("code-5", "title-483")
        try {
            item.moveTo(LegacyUtilizationEstimateStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyUtilizationEstimateStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyUtilizationEstimate("code-5", "title-483")
        item.status = LegacyUtilizationEstimateStatus.FAILED
        assertFails("move") { item.moveTo(LegacyUtilizationEstimateStatus.NEW) }
    }
}
