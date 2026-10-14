package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newDutyPeriodAgreement("code-983", "label-493")
        assertEquals(DutyPeriodAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDutyPeriodAgreement("code-983", "label-493")
        try {
            item.moveTo(DutyPeriodAgreementStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodAgreementStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDutyPeriodAgreement("code-983", "label-493")
        item.status = DutyPeriodAgreementStatus.FAILED
        assertFails("move") { item.moveTo(DutyPeriodAgreementStatus.NEW) }
    }
}
