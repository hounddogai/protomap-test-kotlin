package com.acme.transport.crewing.restbreak.legacybreakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRestBreakAdjustmentTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalRestBreakAdjustment("code-281", "name-156")
        assertEquals(RegionalRestBreakAdjustmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRegionalRestBreakAdjustment("code-281", "name-156")
        try {
            item.moveTo(RegionalRestBreakAdjustmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRestBreakAdjustmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalRestBreakAdjustment("code-281", "name-156")
        item.status = RegionalRestBreakAdjustmentStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalRestBreakAdjustmentStatus.CREATED) }
    }
}
