package com.acme.transport.fleetops.defect.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectCycleTest {
    @Test
    fun `starts in requested`() {
        val item = newDefectCycle("code-583", "label-222")
        assertEquals(DefectCycleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefectCycle("code-583", "label-222")
        try {
            item.moveTo(DefectCycleStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectCycleStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefectCycle("code-583", "label-222")
        item.status = DefectCycleStatus.EXPIRED
        assertFails("move") { item.moveTo(DefectCycleStatus.REQUESTED) }
    }
}
