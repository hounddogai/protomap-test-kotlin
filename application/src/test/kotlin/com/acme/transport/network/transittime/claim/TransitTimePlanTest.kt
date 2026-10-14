package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimePlanTest {
    @Test
    fun `starts in open`() {
        val item = newTransitTimePlan("code-587", "title-560")
        assertEquals(TransitTimePlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newTransitTimePlan("code-587", "title-560")
        try {
            item.moveTo(TransitTimePlanStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimePlanStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTransitTimePlan("code-587", "title-560")
        item.status = TransitTimePlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(TransitTimePlanStatus.OPEN) }
    }
}
