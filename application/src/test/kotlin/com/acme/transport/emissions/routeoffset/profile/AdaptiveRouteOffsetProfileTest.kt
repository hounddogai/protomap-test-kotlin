package com.acme.transport.emissions.routeoffset.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveRouteOffsetProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveRouteOffsetProfile("code-675", "title-679")
        assertEquals(AdaptiveRouteOffsetProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAdaptiveRouteOffsetProfile("code-675", "title-679")
        try {
            item.moveTo(AdaptiveRouteOffsetProfileStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveRouteOffsetProfileStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveRouteOffsetProfile("code-675", "title-679")
        item.status = AdaptiveRouteOffsetProfileStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveRouteOffsetProfileStatus.DRAFT) }
    }
}
