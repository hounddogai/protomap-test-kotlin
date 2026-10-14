package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTrailerClaimTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryTrailerClaim("code-961", "label-888")
        assertEquals(PrimaryTrailerClaimStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newPrimaryTrailerClaim("code-961", "label-888")
        try {
            item.moveTo(PrimaryTrailerClaimStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTrailerClaimStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryTrailerClaim("code-961", "label-888")
        item.status = PrimaryTrailerClaimStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryTrailerClaimStatus.PROPOSED) }
    }
}
