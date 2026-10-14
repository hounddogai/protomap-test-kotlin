package com.acme.transport.fleetops.defect.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDefectCycleTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalDefectCycle("code-832", "label-773")
        assertEquals(RegionalDefectCycleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRegionalDefectCycle("code-832", "label-773")
        try {
            item.moveTo(RegionalDefectCycleStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDefectCycleStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalDefectCycle("code-832", "label-773")
        item.status = RegionalDefectCycleStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalDefectCycleStatus.REQUESTED) }
    }
}
