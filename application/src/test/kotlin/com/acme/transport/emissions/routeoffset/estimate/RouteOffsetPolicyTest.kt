package com.acme.transport.emissions.routeoffset.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newRouteOffsetPolicy("code-486", "name-91")
        assertEquals(RouteOffsetPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newRouteOffsetPolicy("code-486", "name-91")
        try {
            item.moveTo(RouteOffsetPolicyStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetPolicyStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRouteOffsetPolicy("code-486", "name-91")
        item.status = RouteOffsetPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(RouteOffsetPolicyStatus.IDLE) }
    }
}
