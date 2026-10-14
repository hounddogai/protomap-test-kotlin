package com.acme.transport.terminal.dwell.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDwellAdjustmentTest {
    @Test
    fun `starts in open`() {
        val item = newStrictDwellAdjustment("code-817", "label-790")
        assertEquals(StrictDwellAdjustmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newStrictDwellAdjustment("code-817", "label-790")
        try {
            item.moveTo(StrictDwellAdjustmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDwellAdjustmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictDwellAdjustment("code-817", "label-790")
        item.status = StrictDwellAdjustmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictDwellAdjustmentStatus.OPEN) }
    }
}
