package com.acme.transport.linehaul.trailer.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalTrailerPlanTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalTrailerPlan("code-745", "title-275")
        assertEquals(RegionalTrailerPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRegionalTrailerPlan("code-745", "title-275")
        try {
            item.moveTo(RegionalTrailerPlanStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalTrailerPlanStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalTrailerPlan("code-745", "title-275")
        item.status = RegionalTrailerPlanStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalTrailerPlanStatus.CREATED) }
    }
}
