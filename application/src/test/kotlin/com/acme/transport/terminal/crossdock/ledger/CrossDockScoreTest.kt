package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newCrossDockScore("code-871", "label-449")
        assertEquals(CrossDockScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newCrossDockScore("code-871", "label-449")
        try {
            item.moveTo(CrossDockScoreStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockScoreStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCrossDockScore("code-871", "label-449")
        item.status = CrossDockScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CrossDockScoreStatus.PROPOSED) }
    }
}
