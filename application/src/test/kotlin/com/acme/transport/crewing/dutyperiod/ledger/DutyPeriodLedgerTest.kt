package com.acme.transport.crewing.dutyperiod.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newDutyPeriodLedger("code-343", "name-635")
        assertEquals(DutyPeriodLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDutyPeriodLedger("code-343", "name-635")
        try {
            item.moveTo(DutyPeriodLedgerStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodLedgerStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDutyPeriodLedger("code-343", "name-635")
        item.status = DutyPeriodLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(DutyPeriodLedgerStatus.DRAFT) }
    }
}
