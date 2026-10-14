package com.acme.transport.safety.coaching.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingTierTest {
    @Test
    fun `starts in draft`() {
        val item = newCoachingTier("code-276", "title-101")
        assertEquals(CoachingTierStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCoachingTier("code-276", "title-101")
        try {
            item.moveTo(CoachingTierStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingTierStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCoachingTier("code-276", "title-101")
        item.status = CoachingTierStatus.CLOSED
        assertFails("move") { item.moveTo(CoachingTierStatus.DRAFT) }
    }
}
