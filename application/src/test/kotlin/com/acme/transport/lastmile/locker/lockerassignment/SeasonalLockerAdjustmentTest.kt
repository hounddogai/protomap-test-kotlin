package com.acme.transport.lastmile.locker.lockerassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalLockerAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalLockerAdjustment("code-761", "label-148")
        assertEquals(SeasonalLockerAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newSeasonalLockerAdjustment("code-761", "label-148")
        try {
            item.moveTo(SeasonalLockerAdjustmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalLockerAdjustmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalLockerAdjustment("code-761", "label-148")
        item.status = SeasonalLockerAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalLockerAdjustmentStatus.PROPOSED) }
    }
}
