package com.acme.transport.crewing.dutyperiod.periodassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDutyPeriodLedgerTest {
    @Test
    fun `starts in new`() {
        val item = newManualDutyPeriodLedger("code-696", "label-975")
        assertEquals(ManualDutyPeriodLedgerStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newManualDutyPeriodLedger("code-696", "label-975")
        try {
            item.moveTo(ManualDutyPeriodLedgerStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDutyPeriodLedgerStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualDutyPeriodLedger("code-696", "label-975")
        item.status = ManualDutyPeriodLedgerStatus.FAILED
        assertFails("move") { item.moveTo(ManualDutyPeriodLedgerStatus.NEW) }
    }
}
