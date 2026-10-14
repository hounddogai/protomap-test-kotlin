package com.acme.transport.lastmile.locker.lockerrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLockerRuleTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledLockerRule("code-742", "label-846")
        assertEquals(ScheduledLockerRuleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newScheduledLockerRule("code-742", "label-846")
        try {
            item.moveTo(ScheduledLockerRuleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLockerRuleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledLockerRule("code-742", "label-846")
        item.status = ScheduledLockerRuleStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledLockerRuleStatus.DRAFT) }
    }
}
