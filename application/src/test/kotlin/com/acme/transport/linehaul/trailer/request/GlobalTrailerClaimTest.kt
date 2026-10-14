package com.acme.transport.linehaul.trailer.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTrailerClaimTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalTrailerClaim("code-115", "title-424")
        assertEquals(GlobalTrailerClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newGlobalTrailerClaim("code-115", "title-424")
        try {
            item.moveTo(GlobalTrailerClaimStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTrailerClaimStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalTrailerClaim("code-115", "title-424")
        item.status = GlobalTrailerClaimStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalTrailerClaimStatus.CREATED) }
    }
}
