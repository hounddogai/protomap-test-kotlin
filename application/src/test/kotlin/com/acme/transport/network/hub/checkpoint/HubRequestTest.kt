package com.acme.transport.network.hub.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newHubRequest("code-675", "label-434")
        assertEquals(HubRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newHubRequest("code-675", "label-434")
        try {
            item.moveTo(HubRequestStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubRequestStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newHubRequest("code-675", "label-434")
        item.status = HubRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(HubRequestStatus.PROPOSED) }
    }
}
