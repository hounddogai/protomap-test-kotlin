package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedLanePlanTargetTest {
    @Test
    fun `starts in new`() {
        val item = newCappedLanePlanTarget("code-213", "label-434")
        assertEquals(CappedLanePlanTargetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCappedLanePlanTarget("code-213", "label-434")
        try {
            item.moveTo(CappedLanePlanTargetStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedLanePlanTargetStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedLanePlanTarget("code-213", "label-434")
        item.status = CappedLanePlanTargetStatus.FAILED
        assertFails("move") { item.moveTo(CappedLanePlanTargetStatus.NEW) }
    }
}
