package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newHubAllocation("code-768", "name-345")
        assertEquals(HubAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newHubAllocation("code-768", "name-345")
        try {
            item.moveTo(HubAllocationStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubAllocationStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newHubAllocation("code-768", "name-345")
        item.status = HubAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(HubAllocationStatus.PROPOSED) }
    }
}
