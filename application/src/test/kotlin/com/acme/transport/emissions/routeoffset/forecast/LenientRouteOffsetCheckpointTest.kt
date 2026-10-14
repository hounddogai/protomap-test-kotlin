package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRouteOffsetCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newLenientRouteOffsetCheckpoint("code-793", "title-639")
        assertEquals(LenientRouteOffsetCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientRouteOffsetCheckpoint("code-793", "title-639")
        try {
            item.moveTo(LenientRouteOffsetCheckpointStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRouteOffsetCheckpointStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientRouteOffsetCheckpoint("code-793", "title-639")
        item.status = LenientRouteOffsetCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(LenientRouteOffsetCheckpointStatus.CREATED) }
    }
}
