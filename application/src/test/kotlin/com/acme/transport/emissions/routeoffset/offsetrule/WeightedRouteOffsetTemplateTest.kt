package com.acme.transport.emissions.routeoffset.offsetrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRouteOffsetTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedRouteOffsetTemplate("code-252", "title-672")
        assertEquals(WeightedRouteOffsetTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newWeightedRouteOffsetTemplate("code-252", "title-672")
        try {
            item.moveTo(WeightedRouteOffsetTemplateStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRouteOffsetTemplateStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedRouteOffsetTemplate("code-252", "title-672")
        item.status = WeightedRouteOffsetTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedRouteOffsetTemplateStatus.DRAFT) }
    }
}
