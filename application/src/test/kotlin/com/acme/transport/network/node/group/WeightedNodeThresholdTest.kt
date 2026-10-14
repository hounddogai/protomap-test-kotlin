package com.acme.transport.network.node.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedNodeThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedNodeThreshold("code-951", "label-163")
        assertEquals(WeightedNodeThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newWeightedNodeThreshold("code-951", "label-163")
        try {
            item.moveTo(WeightedNodeThresholdStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedNodeThresholdStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedNodeThreshold("code-951", "label-163")
        item.status = WeightedNodeThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedNodeThresholdStatus.PROPOSED) }
    }
}
