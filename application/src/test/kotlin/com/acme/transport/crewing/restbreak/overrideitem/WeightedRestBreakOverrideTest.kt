package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRestBreakOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedRestBreakOverride("code-129", "label-87")
        assertEquals(WeightedRestBreakOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newWeightedRestBreakOverride("code-129", "label-87")
        try {
            item.moveTo(WeightedRestBreakOverrideStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRestBreakOverrideStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedRestBreakOverride("code-129", "label-87")
        item.status = WeightedRestBreakOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedRestBreakOverrideStatus.PLANNED) }
    }
}
