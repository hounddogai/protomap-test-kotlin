package com.acme.transport.safety.nearmiss.missadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedNearMissAdjustmentTest {
    @Test
    fun `starts in created`() {
        val item = newCappedNearMissAdjustment("code-764", "label-648")
        assertEquals(CappedNearMissAdjustmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCappedNearMissAdjustment("code-764", "label-648")
        try {
            item.moveTo(CappedNearMissAdjustmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedNearMissAdjustmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedNearMissAdjustment("code-764", "label-648")
        item.status = CappedNearMissAdjustmentStatus.RETIRED
        assertFails("move") { item.moveTo(CappedNearMissAdjustmentStatus.CREATED) }
    }
}
