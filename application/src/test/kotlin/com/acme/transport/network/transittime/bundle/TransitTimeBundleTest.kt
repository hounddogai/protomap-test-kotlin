package com.acme.transport.network.transittime.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeBundleTest {
    @Test
    fun `starts in planned`() {
        val item = newTransitTimeBundle("code-669", "name-550")
        assertEquals(TransitTimeBundleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTransitTimeBundle("code-669", "name-550")
        try {
            item.moveTo(TransitTimeBundleStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeBundleStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTransitTimeBundle("code-669", "name-550")
        item.status = TransitTimeBundleStatus.CANCELLED
        assertFails("move") { item.moveTo(TransitTimeBundleStatus.PLANNED) }
    }
}
