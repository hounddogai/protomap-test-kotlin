package com.acme.transport.fleetops.defect.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDefectThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalDefectThreshold("code-976", "title-955")
        assertEquals(RegionalDefectThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRegionalDefectThreshold("code-976", "title-955")
        try {
            item.moveTo(RegionalDefectThresholdStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDefectThresholdStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalDefectThreshold("code-976", "title-955")
        item.status = RegionalDefectThresholdStatus.FAILED
        assertFails("move") { item.moveTo(RegionalDefectThresholdStatus.NEW) }
    }
}
