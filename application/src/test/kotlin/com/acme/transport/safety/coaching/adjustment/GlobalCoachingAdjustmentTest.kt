package com.acme.transport.safety.coaching.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCoachingAdjustmentTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalCoachingAdjustment("code-611", "title-355")
        assertEquals(GlobalCoachingAdjustmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newGlobalCoachingAdjustment("code-611", "title-355")
        try {
            item.moveTo(GlobalCoachingAdjustmentStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCoachingAdjustmentStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalCoachingAdjustment("code-611", "title-355")
        item.status = GlobalCoachingAdjustmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalCoachingAdjustmentStatus.OPEN) }
    }
}
