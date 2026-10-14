package com.acme.transport.safety.nearmiss.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientNearMissClaimTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientNearMissClaim("code-534", "title-762")
        assertEquals(LenientNearMissClaimStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLenientNearMissClaim("code-534", "title-762")
        try {
            item.moveTo(LenientNearMissClaimStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientNearMissClaimStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientNearMissClaim("code-534", "title-762")
        item.status = LenientNearMissClaimStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientNearMissClaimStatus.PROPOSED) }
    }
}
