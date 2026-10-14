package com.acme.transport.linehaul.relay.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRelayScoreTest {
    @Test
    fun `starts in new`() {
        val item = newLenientRelayScore("code-283", "label-751")
        assertEquals(LenientRelayScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLenientRelayScore("code-283", "label-751")
        try {
            item.moveTo(LenientRelayScoreStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRelayScoreStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientRelayScore("code-283", "label-751")
        item.status = LenientRelayScoreStatus.FAILED
        assertFails("move") { item.moveTo(LenientRelayScoreStatus.NEW) }
    }
}
