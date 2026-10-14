package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimePreferenceTest {
    @Test
    fun `starts in draft`() {
        val item = newTransitTimePreference("code-817", "name-863")
        assertEquals(TransitTimePreferenceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newTransitTimePreference("code-817", "name-863")
        try {
            item.moveTo(TransitTimePreferenceStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimePreferenceStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTransitTimePreference("code-817", "name-863")
        item.status = TransitTimePreferenceStatus.CLOSED
        assertFails("move") { item.moveTo(TransitTimePreferenceStatus.DRAFT) }
    }
}
