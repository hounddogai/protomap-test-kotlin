package com.acme.transport.safety.coaching.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingLedgerTest {
    @Test
    fun `starts in requested`() {
        val item = newCoachingLedger("code-139", "title-875")
        assertEquals(CoachingLedgerStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCoachingLedger("code-139", "title-875")
        try {
            item.moveTo(CoachingLedgerStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingLedgerStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCoachingLedger("code-139", "title-875")
        item.status = CoachingLedgerStatus.EXPIRED
        assertFails("move") { item.moveTo(CoachingLedgerStatus.REQUESTED) }
    }
}
