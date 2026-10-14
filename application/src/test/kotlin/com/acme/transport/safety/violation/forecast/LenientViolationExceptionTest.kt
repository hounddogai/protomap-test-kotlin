package com.acme.transport.safety.violation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientViolationExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientViolationException("code-613", "title-626")
        assertEquals(LenientViolationExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLenientViolationException("code-613", "title-626")
        try {
            item.moveTo(LenientViolationExceptionStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientViolationExceptionStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientViolationException("code-613", "title-626")
        item.status = LenientViolationExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(LenientViolationExceptionStatus.IDLE) }
    }
}
