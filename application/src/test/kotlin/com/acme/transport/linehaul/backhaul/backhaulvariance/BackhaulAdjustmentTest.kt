package com.acme.transport.linehaul.backhaul.backhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newBackhaulAdjustment("code-988", "name-173")
        assertEquals(BackhaulAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newBackhaulAdjustment("code-988", "name-173")
        try {
            item.moveTo(BackhaulAdjustmentStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulAdjustmentStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBackhaulAdjustment("code-988", "name-173")
        item.status = BackhaulAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(BackhaulAdjustmentStatus.NEW) }
    }
}
