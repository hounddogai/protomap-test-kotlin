package com.acme.transport.linehaul.relay.relaymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRelayRequestTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryRelayRequest("code-582", "title-883")
        assertEquals(PrimaryRelayRequestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPrimaryRelayRequest("code-582", "title-883")
        try {
            item.moveTo(PrimaryRelayRequestStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRelayRequestStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryRelayRequest("code-582", "title-883")
        item.status = PrimaryRelayRequestStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryRelayRequestStatus.CREATED) }
    }
}
