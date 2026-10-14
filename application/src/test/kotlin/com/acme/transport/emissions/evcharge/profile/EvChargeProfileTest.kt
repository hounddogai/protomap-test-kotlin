package com.acme.transport.emissions.evcharge.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeProfileTest {
    @Test
    fun `starts in open`() {
        val item = newEvChargeProfile("code-924", "name-756")
        assertEquals(EvChargeProfileStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newEvChargeProfile("code-924", "name-756")
        try {
            item.moveTo(EvChargeProfileStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeProfileStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newEvChargeProfile("code-924", "name-756")
        item.status = EvChargeProfileStatus.ARCHIVED
        assertFails("move") { item.moveTo(EvChargeProfileStatus.OPEN) }
    }
}
