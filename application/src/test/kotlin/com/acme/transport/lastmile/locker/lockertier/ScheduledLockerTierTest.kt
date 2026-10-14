package com.acme.transport.lastmile.locker.lockertier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLockerTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledLockerTier("code-981", "title-609")
        assertEquals(ScheduledLockerTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newScheduledLockerTier("code-981", "title-609")
        try {
            item.moveTo(ScheduledLockerTierStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLockerTierStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledLockerTier("code-981", "title-609")
        item.status = ScheduledLockerTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledLockerTierStatus.PROPOSED) }
    }
}
