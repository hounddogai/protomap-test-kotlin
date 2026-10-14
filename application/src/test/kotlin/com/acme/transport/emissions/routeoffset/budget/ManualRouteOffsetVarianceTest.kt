package com.acme.transport.emissions.routeoffset.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRouteOffsetVarianceTest {
    @Test
    fun `starts in draft`() {
        val item = newManualRouteOffsetVariance("code-565", "label-837")
        assertEquals(ManualRouteOffsetVarianceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newManualRouteOffsetVariance("code-565", "label-837")
        try {
            item.moveTo(ManualRouteOffsetVarianceStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRouteOffsetVarianceStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualRouteOffsetVariance("code-565", "label-837")
        item.status = ManualRouteOffsetVarianceStatus.CLOSED
        assertFails("move") { item.moveTo(ManualRouteOffsetVarianceStatus.DRAFT) }
    }
}
