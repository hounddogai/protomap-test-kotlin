package com.acme.transport.customs.hold.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newHoldException("code-147", "label-957")
        assertEquals(HoldExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newHoldException("code-147", "label-957")
        try {
            item.moveTo(HoldExceptionStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldExceptionStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newHoldException("code-147", "label-957")
        item.status = HoldExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(HoldExceptionStatus.PLANNED) }
    }
}
