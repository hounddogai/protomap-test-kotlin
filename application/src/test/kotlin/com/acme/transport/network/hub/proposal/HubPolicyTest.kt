package com.acme.transport.network.hub.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newHubPolicy("code-409", "label-742")
        assertEquals(HubPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newHubPolicy("code-409", "label-742")
        try {
            item.moveTo(HubPolicyStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubPolicyStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newHubPolicy("code-409", "label-742")
        item.status = HubPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(HubPolicyStatus.IDLE) }
    }
}
