package com.acme.transport.crewing.dutyperiod.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDutyPeriodPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedDutyPeriodPolicy("code-352", "label-99")
        assertEquals(CappedDutyPeriodPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newCappedDutyPeriodPolicy("code-352", "label-99")
        try {
            item.moveTo(CappedDutyPeriodPolicyStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDutyPeriodPolicyStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedDutyPeriodPolicy("code-352", "label-99")
        item.status = CappedDutyPeriodPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(CappedDutyPeriodPolicyStatus.IDLE) }
    }
}
