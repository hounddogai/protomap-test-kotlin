package com.acme.transport.crewing.restbreak.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRestBreakClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictRestBreakClaim("code-330", "title-662")
        assertEquals(StrictRestBreakClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newStrictRestBreakClaim("code-330", "title-662")
        try {
            item.moveTo(StrictRestBreakClaimStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRestBreakClaimStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictRestBreakClaim("code-330", "title-662")
        item.status = StrictRestBreakClaimStatus.STOPPED
        assertFails("move") { item.moveTo(StrictRestBreakClaimStatus.IDLE) }
    }
}
