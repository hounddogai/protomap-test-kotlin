package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanAdjustmentTest {
    @Test
    fun `starts in draft`() {
        val item = newLanePlanAdjustment("code-54", "name-67")
        assertEquals(LanePlanAdjustmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLanePlanAdjustment("code-54", "name-67")
        try {
            item.moveTo(LanePlanAdjustmentStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanAdjustmentStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLanePlanAdjustment("code-54", "name-67")
        item.status = LanePlanAdjustmentStatus.CLOSED
        assertFails("move") { item.moveTo(LanePlanAdjustmentStatus.DRAFT) }
    }
}
