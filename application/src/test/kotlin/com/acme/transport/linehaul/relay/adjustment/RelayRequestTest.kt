package com.acme.transport.linehaul.relay.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newRelayRequest("code-128", "title-70")
        assertEquals(RelayRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newRelayRequest("code-128", "title-70")
        try {
            item.moveTo(RelayRequestStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayRequestStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRelayRequest("code-128", "title-70")
        item.status = RelayRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RelayRequestStatus.PROPOSED) }
    }
}
