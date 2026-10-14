package com.acme.transport.fleetops.utilization.utilizationrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualUtilizationRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualUtilizationRecord("code-943", "label-567")
        assertEquals(ManualUtilizationRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualUtilizationRecord("code-943", "label-567")
        try {
            item.moveTo(ManualUtilizationRecordStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualUtilizationRecordStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualUtilizationRecord("code-943", "label-567")
        item.status = ManualUtilizationRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualUtilizationRecordStatus.PROPOSED) }
    }
}
