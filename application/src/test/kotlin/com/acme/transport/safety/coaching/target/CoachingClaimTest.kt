package com.acme.transport.safety.coaching.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingClaimTest {
    @Test
    fun `starts in open`() {
        val item = newCoachingClaim("code-403", "title-706")
        assertEquals(CoachingClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newCoachingClaim("code-403", "title-706")
        try {
            item.moveTo(CoachingClaimStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingClaimStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCoachingClaim("code-403", "title-706")
        item.status = CoachingClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(CoachingClaimStatus.OPEN) }
    }
}
