package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCutoffEstimateTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedCutoffEstimate("code-981", "label-340")
        assertEquals(WeightedCutoffEstimateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newWeightedCutoffEstimate("code-981", "label-340")
        try {
            item.moveTo(WeightedCutoffEstimateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCutoffEstimateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedCutoffEstimate("code-981", "label-340")
        item.status = WeightedCutoffEstimateStatus.FAILED
        assertFails("move") { item.moveTo(WeightedCutoffEstimateStatus.NEW) }
    }
}
