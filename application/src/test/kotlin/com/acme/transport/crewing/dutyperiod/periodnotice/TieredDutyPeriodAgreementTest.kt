package com.acme.transport.crewing.dutyperiod.periodnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDutyPeriodAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newTieredDutyPeriodAgreement("code-61", "label-349")
        assertEquals(TieredDutyPeriodAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newTieredDutyPeriodAgreement("code-61", "label-349")
        try {
            item.moveTo(TieredDutyPeriodAgreementStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDutyPeriodAgreementStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredDutyPeriodAgreement("code-61", "label-349")
        item.status = TieredDutyPeriodAgreementStatus.FAILED
        assertFails("move") { item.moveTo(TieredDutyPeriodAgreementStatus.NEW) }
    }
}
