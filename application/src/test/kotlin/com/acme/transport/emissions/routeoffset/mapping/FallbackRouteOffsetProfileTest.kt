package com.acme.transport.emissions.routeoffset.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRouteOffsetProfileTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackRouteOffsetProfile("code-452", "title-579")
        assertEquals(FallbackRouteOffsetProfileStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newFallbackRouteOffsetProfile("code-452", "title-579")
        try {
            item.moveTo(FallbackRouteOffsetProfileStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRouteOffsetProfileStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackRouteOffsetProfile("code-452", "title-579")
        item.status = FallbackRouteOffsetProfileStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackRouteOffsetProfileStatus.OPEN) }
    }
}
