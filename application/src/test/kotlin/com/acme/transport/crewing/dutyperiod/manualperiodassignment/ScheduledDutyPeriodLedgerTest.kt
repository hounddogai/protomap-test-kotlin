package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDutyPeriodLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledDutyPeriodLedger("code-812", "name-208")
        assertEquals(ScheduledDutyPeriodLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledDutyPeriodLedger("code-812", "name-208")
        try {
            item.moveTo(ScheduledDutyPeriodLedgerStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDutyPeriodLedgerStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledDutyPeriodLedger("code-812", "name-208")
        item.status = ScheduledDutyPeriodLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledDutyPeriodLedgerStatus.CREATED) }
    }
}
