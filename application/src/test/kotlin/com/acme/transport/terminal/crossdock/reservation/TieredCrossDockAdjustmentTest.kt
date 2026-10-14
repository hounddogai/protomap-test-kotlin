package com.acme.transport.terminal.crossdock.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCrossDockAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredCrossDockAdjustment("code-415", "name-182")
        assertEquals(TieredCrossDockAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newTieredCrossDockAdjustment("code-415", "name-182")
        try {
            item.moveTo(TieredCrossDockAdjustmentStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCrossDockAdjustmentStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredCrossDockAdjustment("code-415", "name-182")
        item.status = TieredCrossDockAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredCrossDockAdjustmentStatus.PROPOSED) }
    }
}
