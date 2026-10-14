package com.acme.transport.network.node.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveNodeThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveNodeThreshold("code-543", "name-342")
        assertEquals(AdaptiveNodeThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAdaptiveNodeThreshold("code-543", "name-342")
        try {
            item.moveTo(AdaptiveNodeThresholdStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveNodeThresholdStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveNodeThreshold("code-543", "name-342")
        item.status = AdaptiveNodeThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveNodeThresholdStatus.IDLE) }
    }
}
