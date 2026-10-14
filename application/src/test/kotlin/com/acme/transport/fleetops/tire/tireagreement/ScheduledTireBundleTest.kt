package com.acme.transport.fleetops.tire.tireagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTireBundleTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledTireBundle("code-118", "label-564")
        assertEquals(ScheduledTireBundleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newScheduledTireBundle("code-118", "label-564")
        try {
            item.moveTo(ScheduledTireBundleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTireBundleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledTireBundle("code-118", "label-564")
        item.status = ScheduledTireBundleStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledTireBundleStatus.REQUESTED) }
    }
}
