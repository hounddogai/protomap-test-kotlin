package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRouteOffsetCheckpointTest {
    @Test
    fun `starts in open`() {
        val item = newStrictRouteOffsetCheckpoint("code-743", "title-302")
        assertEquals(StrictRouteOffsetCheckpointStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newStrictRouteOffsetCheckpoint("code-743", "title-302")
        try {
            item.moveTo(StrictRouteOffsetCheckpointStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRouteOffsetCheckpointStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictRouteOffsetCheckpoint("code-743", "title-302")
        item.status = StrictRouteOffsetCheckpointStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictRouteOffsetCheckpointStatus.OPEN) }
    }
}
