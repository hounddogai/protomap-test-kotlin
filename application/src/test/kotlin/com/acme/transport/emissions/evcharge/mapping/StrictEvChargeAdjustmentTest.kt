package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictEvChargeAdjustmentTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictEvChargeAdjustment("code-545", "label-463")
        assertEquals(StrictEvChargeAdjustmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newStrictEvChargeAdjustment("code-545", "label-463")
        try {
            item.moveTo(StrictEvChargeAdjustmentStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictEvChargeAdjustmentStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictEvChargeAdjustment("code-545", "label-463")
        item.status = StrictEvChargeAdjustmentStatus.CLOSED
        assertFails("move") { item.moveTo(StrictEvChargeAdjustmentStatus.DRAFT) }
    }
}
