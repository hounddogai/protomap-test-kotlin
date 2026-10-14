package com.acme.transport.lastmile.locker.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedLockerExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedLockerException("code-493", "label-540")
        assertEquals(CappedLockerExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newCappedLockerException("code-493", "label-540")
        try {
            item.moveTo(CappedLockerExceptionStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedLockerExceptionStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedLockerException("code-493", "label-540")
        item.status = CappedLockerExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedLockerExceptionStatus.PLANNED) }
    }
}
