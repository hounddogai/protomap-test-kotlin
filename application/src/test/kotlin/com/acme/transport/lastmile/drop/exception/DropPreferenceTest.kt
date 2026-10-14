package com.acme.transport.lastmile.drop.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropPreferenceTest {
    @Test
    fun `starts in draft`() {
        val item = newDropPreference("code-812", "label-973")
        assertEquals(DropPreferenceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDropPreference("code-812", "label-973")
        try {
            item.moveTo(DropPreferenceStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropPreferenceStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDropPreference("code-812", "label-973")
        item.status = DropPreferenceStatus.CLOSED
        assertFails("move") { item.moveTo(DropPreferenceStatus.DRAFT) }
    }
}
