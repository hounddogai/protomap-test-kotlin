package com.acme.transport.emissions.evcharge.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newEvChargeEstimate("code-661", "name-916")
        assertEquals(EvChargeEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newEvChargeEstimate("code-661", "name-916")
        try {
            item.moveTo(EvChargeEstimateStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeEstimateStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newEvChargeEstimate("code-661", "name-916")
        item.status = EvChargeEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(EvChargeEstimateStatus.PROPOSED) }
    }
}
