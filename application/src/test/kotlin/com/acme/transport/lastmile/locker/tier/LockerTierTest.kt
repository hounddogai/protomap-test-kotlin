package com.acme.transport.lastmile.locker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerTierTest {
    @Test
    fun `starts in draft`() {
        val item = newLockerTier("code-236", "title-529")
        assertEquals(LockerTierStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLockerTier("code-236", "title-529")
        try {
            item.moveTo(LockerTierStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerTierStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLockerTier("code-236", "title-529")
        item.status = LockerTierStatus.CLOSED
        assertFails("move") { item.moveTo(LockerTierStatus.DRAFT) }
    }
}
