package com.acme.transport.linehaul.relay.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRelayWindowTest {
    @Test
    fun `starts in open`() {
        val item = newStrictRelayWindow("code-241", "label-556")
        assertEquals(StrictRelayWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newStrictRelayWindow("code-241", "label-556")
        try {
            item.moveTo(StrictRelayWindowStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRelayWindowStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictRelayWindow("code-241", "label-556")
        item.status = StrictRelayWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictRelayWindowStatus.OPEN) }
    }
}
