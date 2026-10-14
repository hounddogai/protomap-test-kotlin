package com.acme.transport.network.cutoff.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCutoffScoreTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingCutoffScore("code-969", "label-691")
        assertEquals(PendingCutoffScoreStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newPendingCutoffScore("code-969", "label-691")
        try {
            item.moveTo(PendingCutoffScoreStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCutoffScoreStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingCutoffScore("code-969", "label-691")
        item.status = PendingCutoffScoreStatus.STOPPED
        assertFails("move") { item.moveTo(PendingCutoffScoreStatus.IDLE) }
    }
}
