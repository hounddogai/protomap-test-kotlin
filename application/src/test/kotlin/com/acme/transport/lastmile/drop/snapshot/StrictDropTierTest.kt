package com.acme.transport.lastmile.drop.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDropTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictDropTier("code-305", "title-828")
        assertEquals(StrictDropTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newStrictDropTier("code-305", "title-828")
        try {
            item.moveTo(StrictDropTierStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDropTierStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictDropTier("code-305", "title-828")
        item.status = StrictDropTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictDropTierStatus.PROPOSED) }
    }
}
