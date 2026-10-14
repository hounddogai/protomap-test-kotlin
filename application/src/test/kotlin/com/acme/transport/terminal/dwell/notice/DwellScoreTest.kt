package com.acme.transport.terminal.dwell.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newDwellScore("code-787", "label-258")
        assertEquals(DwellScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newDwellScore("code-787", "label-258")
        try {
            item.moveTo(DwellScoreStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellScoreStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDwellScore("code-787", "label-258")
        item.status = DwellScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(DwellScoreStatus.PLANNED) }
    }
}
