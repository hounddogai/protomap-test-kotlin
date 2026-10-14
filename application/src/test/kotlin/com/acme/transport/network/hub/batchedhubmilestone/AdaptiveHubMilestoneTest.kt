package com.acme.transport.network.hub.batchedhubmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveHubMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveHubMilestone("code-670", "label-440")
        assertEquals(AdaptiveHubMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAdaptiveHubMilestone("code-670", "label-440")
        try {
            item.moveTo(AdaptiveHubMilestoneStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveHubMilestoneStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveHubMilestone("code-670", "label-440")
        item.status = AdaptiveHubMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveHubMilestoneStatus.REQUESTED) }
    }
}
