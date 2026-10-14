package com.acme.transport.customs.hold.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredHoldEstimateTest {
    @Test
    fun `starts in new`() {
        val item = newTieredHoldEstimate("code-499", "name-70")
        assertEquals(TieredHoldEstimateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newTieredHoldEstimate("code-499", "name-70")
        try {
            item.moveTo(TieredHoldEstimateStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredHoldEstimateStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredHoldEstimate("code-499", "name-70")
        item.status = TieredHoldEstimateStatus.FAILED
        assertFails("move") { item.moveTo(TieredHoldEstimateStatus.NEW) }
    }
}
