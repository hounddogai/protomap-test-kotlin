package com.acme.transport.fleetops.tire.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newTireGroup("code-552", "name-917")
        assertEquals(TireGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTireGroup("code-552", "name-917")
        try {
            item.moveTo(TireGroupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireGroupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTireGroup("code-552", "name-917")
        item.status = TireGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(TireGroupStatus.REQUESTED) }
    }
}
