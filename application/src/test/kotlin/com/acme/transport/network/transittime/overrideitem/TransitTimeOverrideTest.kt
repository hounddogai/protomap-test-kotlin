package com.acme.transport.network.transittime.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeOverrideTest {
    @Test
    fun `starts in idle`() {
        val item = newTransitTimeOverride("code-380", "name-18")
        assertEquals(TransitTimeOverrideStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTransitTimeOverride("code-380", "name-18")
        try {
            item.moveTo(TransitTimeOverrideStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeOverrideStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTransitTimeOverride("code-380", "name-18")
        item.status = TransitTimeOverrideStatus.STOPPED
        assertFails("move") { item.moveTo(TransitTimeOverrideStatus.IDLE) }
    }
}
