package com.acme.transport.crewing.dutyperiod.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newDutyPeriodThreshold("code-814", "label-258")
        assertEquals(DutyPeriodThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDutyPeriodThreshold("code-814", "label-258")
        try {
            item.moveTo(DutyPeriodThresholdStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodThresholdStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDutyPeriodThreshold("code-814", "label-258")
        item.status = DutyPeriodThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(DutyPeriodThresholdStatus.REQUESTED) }
    }
}
