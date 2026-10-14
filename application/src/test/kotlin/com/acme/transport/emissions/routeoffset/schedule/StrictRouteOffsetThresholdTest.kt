package com.acme.transport.emissions.routeoffset.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRouteOffsetThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictRouteOffsetThreshold("code-22", "title-710")
        assertEquals(StrictRouteOffsetThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newStrictRouteOffsetThreshold("code-22", "title-710")
        try {
            item.moveTo(StrictRouteOffsetThresholdStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRouteOffsetThresholdStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictRouteOffsetThreshold("code-22", "title-710")
        item.status = StrictRouteOffsetThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictRouteOffsetThresholdStatus.REQUESTED) }
    }
}
