package com.acme.transport.safety.nearmiss.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissClaimTest {
    @Test
    fun `starts in proposed`() {
        val item = newNearMissClaim("code-601", "title-238")
        assertEquals(NearMissClaimStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newNearMissClaim("code-601", "title-238")
        try {
            item.moveTo(NearMissClaimStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissClaimStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newNearMissClaim("code-601", "title-238")
        item.status = NearMissClaimStatus.WITHDRAWN
        assertFails("move") { item.moveTo(NearMissClaimStatus.PROPOSED) }
    }
}
