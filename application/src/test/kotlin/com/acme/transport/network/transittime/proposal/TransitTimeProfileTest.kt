package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newTransitTimeProfile("code-790", "name-23")
        assertEquals(TransitTimeProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTransitTimeProfile("code-790", "name-23")
        try {
            item.moveTo(TransitTimeProfileStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeProfileStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTransitTimeProfile("code-790", "name-23")
        item.status = TransitTimeProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(TransitTimeProfileStatus.REQUESTED) }
    }
}
