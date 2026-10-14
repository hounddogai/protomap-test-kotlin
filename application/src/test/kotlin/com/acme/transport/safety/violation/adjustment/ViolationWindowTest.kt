package com.acme.transport.safety.violation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationWindowTest {
    @Test
    fun `starts in idle`() {
        val item = newViolationWindow("code-90", "name-792")
        assertEquals(ViolationWindowStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newViolationWindow("code-90", "name-792")
        try {
            item.moveTo(ViolationWindowStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationWindowStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newViolationWindow("code-90", "name-792")
        item.status = ViolationWindowStatus.STOPPED
        assertFails("move") { item.moveTo(ViolationWindowStatus.IDLE) }
    }
}
