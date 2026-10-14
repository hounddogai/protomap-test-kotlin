package com.acme.transport.emissions.routeoffset.offsetnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRouteOffsetBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultRouteOffsetBaseline("code-294", "label-868")
        assertEquals(DefaultRouteOffsetBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDefaultRouteOffsetBaseline("code-294", "label-868")
        try {
            item.moveTo(DefaultRouteOffsetBaselineStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRouteOffsetBaselineStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultRouteOffsetBaseline("code-294", "label-868")
        item.status = DefaultRouteOffsetBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultRouteOffsetBaselineStatus.IDLE) }
    }
}
