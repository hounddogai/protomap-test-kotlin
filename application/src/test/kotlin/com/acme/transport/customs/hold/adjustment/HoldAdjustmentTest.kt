package com.acme.transport.customs.hold.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldAdjustmentTest {
    @Test
    fun `starts in created`() {
        val item = newHoldAdjustment("code-474", "label-272")
        assertEquals(HoldAdjustmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newHoldAdjustment("code-474", "label-272")
        try {
            item.moveTo(HoldAdjustmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldAdjustmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newHoldAdjustment("code-474", "label-272")
        item.status = HoldAdjustmentStatus.RETIRED
        assertFails("move") { item.moveTo(HoldAdjustmentStatus.CREATED) }
    }
}
