package com.acme.transport.safety.coaching.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCoachingExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newStrictCoachingException("code-641", "label-105")
        assertEquals(StrictCoachingExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newStrictCoachingException("code-641", "label-105")
        try {
            item.moveTo(StrictCoachingExceptionStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCoachingExceptionStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictCoachingException("code-641", "label-105")
        item.status = StrictCoachingExceptionStatus.FAILED
        assertFails("move") { item.moveTo(StrictCoachingExceptionStatus.NEW) }
    }
}
