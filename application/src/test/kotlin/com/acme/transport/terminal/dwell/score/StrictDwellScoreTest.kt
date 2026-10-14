package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDwellScoreTest {
    @Test
    fun `starts in new`() {
        val item = newStrictDwellScore("code-31", "name-900")
        assertEquals(StrictDwellScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newStrictDwellScore("code-31", "name-900")
        try {
            item.moveTo(StrictDwellScoreStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDwellScoreStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictDwellScore("code-31", "name-900")
        item.status = StrictDwellScoreStatus.FAILED
        assertFails("move") { item.moveTo(StrictDwellScoreStatus.NEW) }
    }
}
