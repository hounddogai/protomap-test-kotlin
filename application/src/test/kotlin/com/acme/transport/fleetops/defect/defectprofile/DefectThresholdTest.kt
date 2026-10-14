package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newDefectThreshold("code-299", "title-845")
        assertEquals(DefectThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newDefectThreshold("code-299", "title-845")
        try {
            item.moveTo(DefectThresholdStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectThresholdStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefectThreshold("code-299", "title-845")
        item.status = DefectThresholdStatus.FAILED
        assertFails("move") { item.moveTo(DefectThresholdStatus.NEW) }
    }
}
