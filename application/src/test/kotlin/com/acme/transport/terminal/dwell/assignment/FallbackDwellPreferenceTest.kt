package com.acme.transport.terminal.dwell.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDwellPreferenceTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackDwellPreference("code-492", "name-171")
        assertEquals(FallbackDwellPreferenceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newFallbackDwellPreference("code-492", "name-171")
        try {
            item.moveTo(FallbackDwellPreferenceStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDwellPreferenceStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackDwellPreference("code-492", "name-171")
        item.status = FallbackDwellPreferenceStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackDwellPreferenceStatus.DRAFT) }
    }
}
