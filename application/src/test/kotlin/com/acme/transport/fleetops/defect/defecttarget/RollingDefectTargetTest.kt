package com.acme.transport.fleetops.defect.defecttarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDefectTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingDefectTarget("code-803", "title-361")
        assertEquals(RollingDefectTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRollingDefectTarget("code-803", "title-361")
        try {
            item.moveTo(RollingDefectTargetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDefectTargetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingDefectTarget("code-803", "title-361")
        item.status = RollingDefectTargetStatus.STOPPED
        assertFails("move") { item.moveTo(RollingDefectTargetStatus.IDLE) }
    }
}
