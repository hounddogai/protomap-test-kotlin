package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptWindowTest {
    @Test
    fun `starts in idle`() {
        val item = newAttemptWindow("code-190", "label-728")
        assertEquals(AttemptWindowStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAttemptWindow("code-190", "label-728")
        try {
            item.moveTo(AttemptWindowStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptWindowStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAttemptWindow("code-190", "label-728")
        item.status = AttemptWindowStatus.STOPPED
        assertFails("move") { item.moveTo(AttemptWindowStatus.IDLE) }
    }
}
