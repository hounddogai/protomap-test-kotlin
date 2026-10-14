package com.acme.transport.emissions.idling.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingExceptionTest {
    @Test
    fun `starts in draft`() {
        val item = newIdlingException("code-610", "label-491")
        assertEquals(IdlingExceptionStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newIdlingException("code-610", "label-491")
        try {
            item.moveTo(IdlingExceptionStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingExceptionStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newIdlingException("code-610", "label-491")
        item.status = IdlingExceptionStatus.CLOSED
        assertFails("move") { item.moveTo(IdlingExceptionStatus.DRAFT) }
    }
}
