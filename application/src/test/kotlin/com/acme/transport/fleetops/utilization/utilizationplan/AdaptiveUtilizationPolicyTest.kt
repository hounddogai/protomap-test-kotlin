package com.acme.transport.fleetops.utilization.utilizationplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveUtilizationPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveUtilizationPolicy("code-45", "label-328")
        assertEquals(AdaptiveUtilizationPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newAdaptiveUtilizationPolicy("code-45", "label-328")
        try {
            item.moveTo(AdaptiveUtilizationPolicyStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveUtilizationPolicyStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveUtilizationPolicy("code-45", "label-328")
        item.status = AdaptiveUtilizationPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveUtilizationPolicyStatus.IDLE) }
    }
}
