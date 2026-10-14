package com.acme.transport.lastmile.locker.lockerbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalLockerRuleTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalLockerRule("code-617", "title-813")
        assertEquals(SeasonalLockerRuleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newSeasonalLockerRule("code-617", "title-813")
        try {
            item.moveTo(SeasonalLockerRuleStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalLockerRuleStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalLockerRule("code-617", "title-813")
        item.status = SeasonalLockerRuleStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalLockerRuleStatus.NEW) }
    }
}
