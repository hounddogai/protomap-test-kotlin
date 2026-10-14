package com.acme.transport.crewing.dutyperiod.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDutyPeriodSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackDutyPeriodSnapshot("code-168", "label-254")
        assertEquals(FallbackDutyPeriodSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newFallbackDutyPeriodSnapshot("code-168", "label-254")
        try {
            item.moveTo(FallbackDutyPeriodSnapshotStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDutyPeriodSnapshotStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackDutyPeriodSnapshot("code-168", "label-254")
        item.status = FallbackDutyPeriodSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(FallbackDutyPeriodSnapshotStatus.NEW) }
    }
}
