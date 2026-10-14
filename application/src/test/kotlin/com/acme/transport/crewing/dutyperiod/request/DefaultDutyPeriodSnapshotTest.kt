package com.acme.transport.crewing.dutyperiod.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDutyPeriodSnapshotTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultDutyPeriodSnapshot("code-586", "label-816")
        assertEquals(DefaultDutyPeriodSnapshotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefaultDutyPeriodSnapshot("code-586", "label-816")
        try {
            item.moveTo(DefaultDutyPeriodSnapshotStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDutyPeriodSnapshotStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultDutyPeriodSnapshot("code-586", "label-816")
        item.status = DefaultDutyPeriodSnapshotStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultDutyPeriodSnapshotStatus.REQUESTED) }
    }
}
