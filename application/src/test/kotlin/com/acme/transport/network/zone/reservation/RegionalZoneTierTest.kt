package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalZoneTierTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalZoneTier("code-642", "label-610")
        assertEquals(RegionalZoneTierStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRegionalZoneTier("code-642", "label-610")
        try {
            item.moveTo(RegionalZoneTierStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalZoneTierStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalZoneTier("code-642", "label-610")
        item.status = RegionalZoneTierStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalZoneTierStatus.REQUESTED) }
    }
}
