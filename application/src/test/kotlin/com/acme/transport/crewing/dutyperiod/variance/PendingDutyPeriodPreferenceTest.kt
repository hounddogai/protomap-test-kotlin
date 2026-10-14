package com.acme.transport.crewing.dutyperiod.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDutyPeriodPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingDutyPeriodPreference("code-20", "label-162")
        assertEquals(PendingDutyPeriodPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingDutyPeriodPreference("code-20", "label-162")
        try {
            item.moveTo(PendingDutyPeriodPreferenceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDutyPeriodPreferenceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingDutyPeriodPreference("code-20", "label-162")
        item.status = PendingDutyPeriodPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingDutyPeriodPreferenceStatus.PLANNED) }
    }
}
