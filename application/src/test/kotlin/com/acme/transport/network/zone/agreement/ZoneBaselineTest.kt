package com.acme.transport.network.zone.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newZoneBaseline("code-959", "label-241")
        assertEquals(ZoneBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newZoneBaseline("code-959", "label-241")
        try {
            item.moveTo(ZoneBaselineStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneBaselineStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newZoneBaseline("code-959", "label-241")
        item.status = ZoneBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(ZoneBaselineStatus.DRAFT) }
    }
}
