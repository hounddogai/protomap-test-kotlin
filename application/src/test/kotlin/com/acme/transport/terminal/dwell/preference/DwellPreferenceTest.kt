package com.acme.transport.terminal.dwell.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellPreferenceTest {
    @Test
    fun `starts in draft`() {
        val item = newDwellPreference("code-192", "label-924")
        assertEquals(DwellPreferenceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDwellPreference("code-192", "label-924")
        try {
            item.moveTo(DwellPreferenceStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellPreferenceStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDwellPreference("code-192", "label-924")
        item.status = DwellPreferenceStatus.CLOSED
        assertFails("move") { item.moveTo(DwellPreferenceStatus.DRAFT) }
    }
}
