package com.acme.transport.crewing.dutyperiod.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDutyPeriodSnapshotTest {
    @Test
    fun `starts in open`() {
        val item = newStrictDutyPeriodSnapshot("code-90", "name-802")
        assertEquals(StrictDutyPeriodSnapshotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newStrictDutyPeriodSnapshot("code-90", "name-802")
        try {
            item.moveTo(StrictDutyPeriodSnapshotStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDutyPeriodSnapshotStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictDutyPeriodSnapshot("code-90", "name-802")
        item.status = StrictDutyPeriodSnapshotStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictDutyPeriodSnapshotStatus.OPEN) }
    }
}
