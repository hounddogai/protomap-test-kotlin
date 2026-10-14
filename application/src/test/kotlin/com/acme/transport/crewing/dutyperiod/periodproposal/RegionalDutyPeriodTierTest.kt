package com.acme.transport.crewing.dutyperiod.periodproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDutyPeriodTierTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalDutyPeriodTier("code-211", "name-555")
        assertEquals(RegionalDutyPeriodTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRegionalDutyPeriodTier("code-211", "name-555")
        try {
            item.moveTo(RegionalDutyPeriodTierStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDutyPeriodTierStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalDutyPeriodTier("code-211", "name-555")
        item.status = RegionalDutyPeriodTierStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalDutyPeriodTierStatus.PLANNED) }
    }
}
