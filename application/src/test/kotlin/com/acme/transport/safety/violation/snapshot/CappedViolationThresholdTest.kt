package com.acme.transport.safety.violation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedViolationThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedViolationThreshold("code-495", "title-761")
        assertEquals(CappedViolationThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCappedViolationThreshold("code-495", "title-761")
        try {
            item.moveTo(CappedViolationThresholdStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedViolationThresholdStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedViolationThreshold("code-495", "title-761")
        item.status = CappedViolationThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(CappedViolationThresholdStatus.IDLE) }
    }
}
