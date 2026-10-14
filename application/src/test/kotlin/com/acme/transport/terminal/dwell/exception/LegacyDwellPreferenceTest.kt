package com.acme.transport.terminal.dwell.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDwellPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyDwellPreference("code-579", "name-199")
        assertEquals(LegacyDwellPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newLegacyDwellPreference("code-579", "name-199")
        try {
            item.moveTo(LegacyDwellPreferenceStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDwellPreferenceStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyDwellPreference("code-579", "name-199")
        item.status = LegacyDwellPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyDwellPreferenceStatus.PLANNED) }
    }
}
