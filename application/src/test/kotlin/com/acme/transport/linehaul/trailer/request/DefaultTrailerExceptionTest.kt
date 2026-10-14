package com.acme.transport.linehaul.trailer.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTrailerExceptionTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultTrailerException("code-318", "label-485")
        assertEquals(DefaultTrailerExceptionStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newDefaultTrailerException("code-318", "label-485")
        try {
            item.moveTo(DefaultTrailerExceptionStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTrailerExceptionStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultTrailerException("code-318", "label-485")
        item.status = DefaultTrailerExceptionStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultTrailerExceptionStatus.PROPOSED) }
    }
}
