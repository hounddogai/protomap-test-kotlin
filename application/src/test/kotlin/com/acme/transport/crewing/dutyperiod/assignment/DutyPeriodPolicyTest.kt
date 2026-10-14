package com.acme.transport.crewing.dutyperiod.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodPolicyTest {
    @Test
    fun `starts in draft`() {
        val item = newDutyPeriodPolicy("code-797", "label-968")
        assertEquals(DutyPeriodPolicyStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDutyPeriodPolicy("code-797", "label-968")
        try {
            item.moveTo(DutyPeriodPolicyStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodPolicyStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDutyPeriodPolicy("code-797", "label-968")
        item.status = DutyPeriodPolicyStatus.CLOSED
        assertFails("move") { item.moveTo(DutyPeriodPolicyStatus.DRAFT) }
    }
}
