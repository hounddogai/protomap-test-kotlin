package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newLockerException("code-479", "name-401")
        assertEquals(LockerExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newLockerException("code-479", "name-401")
        try {
            item.moveTo(LockerExceptionStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerExceptionStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLockerException("code-479", "name-401")
        item.status = LockerExceptionStatus.FAILED
        assertFails("move") { item.moveTo(LockerExceptionStatus.NEW) }
    }
}
