package com.acme.transport.crewing.dutyperiod.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDutyPeriodPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newPendingDutyPeriodPolicy("code-681", "title-979")
        assertEquals(PendingDutyPeriodPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newPendingDutyPeriodPolicy("code-681", "title-979")
        try {
            item.moveTo(PendingDutyPeriodPolicyStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDutyPeriodPolicyStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingDutyPeriodPolicy("code-681", "title-979")
        item.status = PendingDutyPeriodPolicyStatus.FAILED
        assertFails("move") { item.moveTo(PendingDutyPeriodPolicyStatus.NEW) }
    }
}
