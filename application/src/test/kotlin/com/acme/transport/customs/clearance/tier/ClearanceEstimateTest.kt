package com.acme.transport.customs.clearance.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newClearanceEstimate("code-774", "label-759")
        assertEquals(ClearanceEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newClearanceEstimate("code-774", "label-759")
        try {
            item.moveTo(ClearanceEstimateStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceEstimateStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newClearanceEstimate("code-774", "label-759")
        item.status = ClearanceEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(ClearanceEstimateStatus.REQUESTED) }
    }
}
