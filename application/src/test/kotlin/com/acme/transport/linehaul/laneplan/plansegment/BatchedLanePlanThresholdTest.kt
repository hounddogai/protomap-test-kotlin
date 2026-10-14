package com.acme.transport.linehaul.laneplan.plansegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedLanePlanThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedLanePlanThreshold("code-677", "name-699")
        assertEquals(BatchedLanePlanThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newBatchedLanePlanThreshold("code-677", "name-699")
        try {
            item.moveTo(BatchedLanePlanThresholdStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedLanePlanThresholdStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedLanePlanThreshold("code-677", "name-699")
        item.status = BatchedLanePlanThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedLanePlanThresholdStatus.PROPOSED) }
    }
}
