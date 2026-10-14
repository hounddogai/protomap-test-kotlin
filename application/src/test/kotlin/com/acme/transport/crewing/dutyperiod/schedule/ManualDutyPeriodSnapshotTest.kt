package com.acme.transport.crewing.dutyperiod.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDutyPeriodSnapshotTest {
    @Test
    fun `starts in planned`() {
        val item = newManualDutyPeriodSnapshot("code-837", "name-527")
        assertEquals(ManualDutyPeriodSnapshotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newManualDutyPeriodSnapshot("code-837", "name-527")
        try {
            item.moveTo(ManualDutyPeriodSnapshotStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDutyPeriodSnapshotStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualDutyPeriodSnapshot("code-837", "name-527")
        item.status = ManualDutyPeriodSnapshotStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualDutyPeriodSnapshotStatus.PLANNED) }
    }
}
