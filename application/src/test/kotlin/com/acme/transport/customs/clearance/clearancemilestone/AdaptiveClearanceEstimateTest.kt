package com.acme.transport.customs.clearance.clearancemilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveClearanceEstimateTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveClearanceEstimate("code-926", "label-703")
        assertEquals(AdaptiveClearanceEstimateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAdaptiveClearanceEstimate("code-926", "label-703")
        try {
            item.moveTo(AdaptiveClearanceEstimateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveClearanceEstimateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveClearanceEstimate("code-926", "label-703")
        item.status = AdaptiveClearanceEstimateStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveClearanceEstimateStatus.NEW) }
    }
}
