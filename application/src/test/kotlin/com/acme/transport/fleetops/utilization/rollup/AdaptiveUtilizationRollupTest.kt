package com.acme.transport.fleetops.utilization.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveUtilizationRollupTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveUtilizationRollup("code-814", "name-539")
        assertEquals(AdaptiveUtilizationRollupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newAdaptiveUtilizationRollup("code-814", "name-539")
        try {
            item.moveTo(AdaptiveUtilizationRollupStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveUtilizationRollupStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveUtilizationRollup("code-814", "name-539")
        item.status = AdaptiveUtilizationRollupStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveUtilizationRollupStatus.NEW) }
    }
}
