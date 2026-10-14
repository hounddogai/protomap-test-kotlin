package com.acme.transport.network.servicearea.areabatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalServiceAreaEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalServiceAreaEstimate("code-347", "label-300")
        assertEquals(RegionalServiceAreaEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRegionalServiceAreaEstimate("code-347", "label-300")
        try {
            item.moveTo(RegionalServiceAreaEstimateStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalServiceAreaEstimateStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalServiceAreaEstimate("code-347", "label-300")
        item.status = RegionalServiceAreaEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalServiceAreaEstimateStatus.PROPOSED) }
    }
}
