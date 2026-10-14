package com.acme.transport.safety.nearmiss.missadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledNearMissQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledNearMissQuota("code-515", "name-201")
        assertEquals(ScheduledNearMissQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newScheduledNearMissQuota("code-515", "name-201")
        try {
            item.moveTo(ScheduledNearMissQuotaStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledNearMissQuotaStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledNearMissQuota("code-515", "name-201")
        item.status = ScheduledNearMissQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledNearMissQuotaStatus.DRAFT) }
    }
}
