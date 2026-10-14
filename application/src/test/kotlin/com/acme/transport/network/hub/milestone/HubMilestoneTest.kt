package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newHubMilestone("code-583", "title-102")
        assertEquals(HubMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newHubMilestone("code-583", "title-102")
        try {
            item.moveTo(HubMilestoneStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubMilestoneStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newHubMilestone("code-583", "title-102")
        item.status = HubMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(HubMilestoneStatus.REQUESTED) }
    }
}
