package com.acme.transport.linehaul.trailer.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerExceptionTest {
    @Test
    fun `starts in proposed`() {
        val item = newTrailerException("code-818", "label-817")
        assertEquals(TrailerExceptionStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newTrailerException("code-818", "label-817")
        try {
            item.moveTo(TrailerExceptionStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerExceptionStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTrailerException("code-818", "label-817")
        item.status = TrailerExceptionStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TrailerExceptionStatus.PROPOSED) }
    }
}
