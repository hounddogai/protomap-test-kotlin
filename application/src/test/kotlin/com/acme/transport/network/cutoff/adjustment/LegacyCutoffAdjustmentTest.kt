package com.acme.transport.network.cutoff.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCutoffAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyCutoffAdjustment("code-202", "title-898")
        assertEquals(LegacyCutoffAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newLegacyCutoffAdjustment("code-202", "title-898")
        try {
            item.moveTo(LegacyCutoffAdjustmentStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCutoffAdjustmentStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyCutoffAdjustment("code-202", "title-898")
        item.status = LegacyCutoffAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(LegacyCutoffAdjustmentStatus.NEW) }
    }
}
