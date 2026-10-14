package com.acme.transport.fleetops.utilization.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedUtilizationEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newCappedUtilizationEstimate("code-992", "label-945")
        assertEquals(CappedUtilizationEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCappedUtilizationEstimate("code-992", "label-945")
        try {
            item.moveTo(CappedUtilizationEstimateStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedUtilizationEstimateStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedUtilizationEstimate("code-992", "label-945")
        item.status = CappedUtilizationEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedUtilizationEstimateStatus.OPEN) }
    }
}
