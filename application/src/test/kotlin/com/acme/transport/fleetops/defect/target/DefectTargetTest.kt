package com.acme.transport.fleetops.defect.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newDefectTarget("code-505", "title-154")
        assertEquals(DefectTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newDefectTarget("code-505", "title-154")
        try {
            item.moveTo(DefectTargetStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectTargetStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefectTarget("code-505", "title-154")
        item.status = DefectTargetStatus.STOPPED
        assertFails("move") { item.moveTo(DefectTargetStatus.IDLE) }
    }
}
