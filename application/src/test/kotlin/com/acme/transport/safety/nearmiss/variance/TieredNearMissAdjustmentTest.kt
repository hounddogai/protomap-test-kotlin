package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredNearMissAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredNearMissAdjustment("code-993", "name-516")
        assertEquals(TieredNearMissAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredNearMissAdjustment("code-993", "name-516")
        try {
            item.moveTo(TieredNearMissAdjustmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredNearMissAdjustmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredNearMissAdjustment("code-993", "name-516")
        item.status = TieredNearMissAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredNearMissAdjustmentStatus.PROPOSED) }
    }
}
