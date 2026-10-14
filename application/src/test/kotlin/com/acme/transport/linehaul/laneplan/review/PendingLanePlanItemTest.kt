package com.acme.transport.linehaul.laneplan.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingLanePlanItemTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingLanePlanItem("code-72", "name-162")
        assertEquals(PendingLanePlanItemStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newPendingLanePlanItem("code-72", "name-162")
        try {
            item.moveTo(PendingLanePlanItemStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingLanePlanItemStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingLanePlanItem("code-72", "name-162")
        item.status = PendingLanePlanItemStatus.STOPPED
        assertFails("move") { item.moveTo(PendingLanePlanItemStatus.IDLE) }
    }
}
