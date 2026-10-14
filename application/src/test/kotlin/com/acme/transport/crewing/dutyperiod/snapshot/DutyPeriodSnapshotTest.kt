package com.acme.transport.crewing.dutyperiod.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodSnapshotTest {
    @Test
    fun `starts in proposed`() {
        val item = newDutyPeriodSnapshot("code-200", "title-914")
        assertEquals(DutyPeriodSnapshotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDutyPeriodSnapshot("code-200", "title-914")
        try {
            item.moveTo(DutyPeriodSnapshotStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodSnapshotStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDutyPeriodSnapshot("code-200", "title-914")
        item.status = DutyPeriodSnapshotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DutyPeriodSnapshotStatus.PROPOSED) }
    }
}
