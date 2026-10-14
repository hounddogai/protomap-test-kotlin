package com.acme.transport.terminal.dwell.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDwellEntryTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveDwellEntry("code-214", "name-51")
        assertEquals(AdaptiveDwellEntryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newAdaptiveDwellEntry("code-214", "name-51")
        try {
            item.moveTo(AdaptiveDwellEntryStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDwellEntryStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveDwellEntry("code-214", "name-51")
        item.status = AdaptiveDwellEntryStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveDwellEntryStatus.PLANNED) }
    }
}
