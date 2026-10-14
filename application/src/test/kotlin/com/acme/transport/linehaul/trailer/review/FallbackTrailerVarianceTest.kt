package com.acme.transport.linehaul.trailer.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTrailerVarianceTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackTrailerVariance("code-889", "name-943")
        assertEquals(FallbackTrailerVarianceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFallbackTrailerVariance("code-889", "name-943")
        try {
            item.moveTo(FallbackTrailerVarianceStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTrailerVarianceStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackTrailerVariance("code-889", "name-943")
        item.status = FallbackTrailerVarianceStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackTrailerVarianceStatus.DRAFT) }
    }
}
