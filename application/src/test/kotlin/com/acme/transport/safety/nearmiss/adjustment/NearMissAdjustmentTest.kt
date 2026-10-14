package com.acme.transport.safety.nearmiss.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newNearMissAdjustment("code-100", "title-557")
        assertEquals(NearMissAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newNearMissAdjustment("code-100", "title-557")
        try {
            item.moveTo(NearMissAdjustmentStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissAdjustmentStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newNearMissAdjustment("code-100", "title-557")
        item.status = NearMissAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(NearMissAdjustmentStatus.NEW) }
    }
}
