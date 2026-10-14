package com.acme.transport.terminal.crossdock.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCrossDockScoreTest {
    @Test
    fun `starts in open`() {
        val item = newPendingCrossDockScore("code-42", "title-107")
        assertEquals(PendingCrossDockScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newPendingCrossDockScore("code-42", "title-107")
        try {
            item.moveTo(PendingCrossDockScoreStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCrossDockScoreStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingCrossDockScore("code-42", "title-107")
        item.status = PendingCrossDockScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingCrossDockScoreStatus.OPEN) }
    }
}
