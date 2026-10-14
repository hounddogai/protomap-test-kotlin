package com.acme.transport.terminal.dwell.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDwellScoreTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackDwellScore("code-650", "name-640")
        assertEquals(FallbackDwellScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newFallbackDwellScore("code-650", "name-640")
        try {
            item.moveTo(FallbackDwellScoreStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDwellScoreStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackDwellScore("code-650", "name-640")
        item.status = FallbackDwellScoreStatus.FAILED
        assertFails("move") { item.moveTo(FallbackDwellScoreStatus.NEW) }
    }
}
