package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newClearanceException("code-260", "name-910")
        assertEquals(ClearanceExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newClearanceException("code-260", "name-910")
        try {
            item.moveTo(ClearanceExceptionStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceExceptionStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newClearanceException("code-260", "name-910")
        item.status = ClearanceExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(ClearanceExceptionStatus.IDLE) }
    }
}
