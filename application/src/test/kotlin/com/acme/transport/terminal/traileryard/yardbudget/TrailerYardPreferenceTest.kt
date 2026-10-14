package com.acme.transport.terminal.traileryard.yardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardPreferenceTest {
    @Test
    fun `starts in draft`() {
        val item = newTrailerYardPreference("code-80", "label-778")
        assertEquals(TrailerYardPreferenceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newTrailerYardPreference("code-80", "label-778")
        try {
            item.moveTo(TrailerYardPreferenceStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardPreferenceStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTrailerYardPreference("code-80", "label-778")
        item.status = TrailerYardPreferenceStatus.CLOSED
        assertFails("move") { item.moveTo(TrailerYardPreferenceStatus.DRAFT) }
    }
}
