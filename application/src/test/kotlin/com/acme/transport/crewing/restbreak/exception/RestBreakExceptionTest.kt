package com.acme.transport.crewing.restbreak.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newRestBreakException("code-126", "label-376")
        assertEquals(RestBreakExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRestBreakException("code-126", "label-376")
        try {
            item.moveTo(RestBreakExceptionStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakExceptionStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRestBreakException("code-126", "label-376")
        item.status = RestBreakExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(RestBreakExceptionStatus.PLANNED) }
    }
}
