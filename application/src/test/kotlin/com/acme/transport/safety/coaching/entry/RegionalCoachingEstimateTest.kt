package com.acme.transport.safety.coaching.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCoachingEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalCoachingEstimate("code-77", "name-4")
        assertEquals(RegionalCoachingEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newRegionalCoachingEstimate("code-77", "name-4")
        try {
            item.moveTo(RegionalCoachingEstimateStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCoachingEstimateStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalCoachingEstimate("code-77", "name-4")
        item.status = RegionalCoachingEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalCoachingEstimateStatus.CREATED) }
    }
}
