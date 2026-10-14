package com.acme.transport.crewing.restbreak.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRestBreakScoreTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultRestBreakScore("code-195", "label-928")
        assertEquals(DefaultRestBreakScoreStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDefaultRestBreakScore("code-195", "label-928")
        try {
            item.moveTo(DefaultRestBreakScoreStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRestBreakScoreStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultRestBreakScore("code-195", "label-928")
        item.status = DefaultRestBreakScoreStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultRestBreakScoreStatus.IDLE) }
    }
}
