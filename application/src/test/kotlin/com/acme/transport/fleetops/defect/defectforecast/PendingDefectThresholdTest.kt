package com.acme.transport.fleetops.defect.defectforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDefectThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingDefectThreshold("code-481", "label-723")
        assertEquals(PendingDefectThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingDefectThreshold("code-481", "label-723")
        try {
            item.moveTo(PendingDefectThresholdStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDefectThresholdStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingDefectThreshold("code-481", "label-723")
        item.status = PendingDefectThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingDefectThresholdStatus.REQUESTED) }
    }
}
