package com.acme.transport.emissions.routeoffset.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newRouteOffsetVariance("code-555", "name-798")
        assertEquals(RouteOffsetVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRouteOffsetVariance("code-555", "name-798")
        try {
            item.moveTo(RouteOffsetVarianceStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetVarianceStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRouteOffsetVariance("code-555", "name-798")
        item.status = RouteOffsetVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(RouteOffsetVarianceStatus.OPEN) }
    }
}
