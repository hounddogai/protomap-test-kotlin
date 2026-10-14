package com.acme.transport.terminal.sortation.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalSortationAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalSortationAdjustment("code-166", "title-816")
        assertEquals(RegionalSortationAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRegionalSortationAdjustment("code-166", "title-816")
        try {
            item.moveTo(RegionalSortationAdjustmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalSortationAdjustmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalSortationAdjustment("code-166", "title-816")
        item.status = RegionalSortationAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalSortationAdjustmentStatus.PROPOSED) }
    }
}
