package com.acme.transport.terminal.sortation.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingSortationWindowTest {
    @Test
    fun `starts in open`() {
        val item = newRollingSortationWindow("code-727", "name-241")
        assertEquals(RollingSortationWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRollingSortationWindow("code-727", "name-241")
        try {
            item.moveTo(RollingSortationWindowStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingSortationWindowStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingSortationWindow("code-727", "name-241")
        item.status = RollingSortationWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingSortationWindowStatus.OPEN) }
    }
}
