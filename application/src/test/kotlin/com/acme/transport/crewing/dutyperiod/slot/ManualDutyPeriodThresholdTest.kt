package com.acme.transport.crewing.dutyperiod.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDutyPeriodThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newManualDutyPeriodThreshold("code-725", "label-246")
        assertEquals(ManualDutyPeriodThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newManualDutyPeriodThreshold("code-725", "label-246")
        try {
            item.moveTo(ManualDutyPeriodThresholdStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDutyPeriodThresholdStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualDutyPeriodThreshold("code-725", "label-246")
        item.status = ManualDutyPeriodThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualDutyPeriodThresholdStatus.REQUESTED) }
    }
}
