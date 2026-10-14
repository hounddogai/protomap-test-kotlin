package com.acme.transport.crewing.dutyperiod.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDutyPeriodProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalDutyPeriodProfile("code-164", "name-619")
        assertEquals(RegionalDutyPeriodProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRegionalDutyPeriodProfile("code-164", "name-619")
        try {
            item.moveTo(RegionalDutyPeriodProfileStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDutyPeriodProfileStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalDutyPeriodProfile("code-164", "name-619")
        item.status = RegionalDutyPeriodProfileStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalDutyPeriodProfileStatus.DRAFT) }
    }
}
