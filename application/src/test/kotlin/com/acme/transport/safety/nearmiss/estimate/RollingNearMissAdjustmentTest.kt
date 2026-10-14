package com.acme.transport.safety.nearmiss.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingNearMissAdjustmentTest {
    @Test
    fun `starts in created`() {
        val item = newRollingNearMissAdjustment("code-576", "label-762")
        assertEquals(RollingNearMissAdjustmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRollingNearMissAdjustment("code-576", "label-762")
        try {
            item.moveTo(RollingNearMissAdjustmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingNearMissAdjustmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingNearMissAdjustment("code-576", "label-762")
        item.status = RollingNearMissAdjustmentStatus.RETIRED
        assertFails("move") { item.moveTo(RollingNearMissAdjustmentStatus.CREATED) }
    }
}
