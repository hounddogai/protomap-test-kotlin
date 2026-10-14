package com.acme.transport.linehaul.tender.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTenderProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyTenderProfile("code-553", "label-650")
        assertEquals(LegacyTenderProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLegacyTenderProfile("code-553", "label-650")
        try {
            item.moveTo(LegacyTenderProfileStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTenderProfileStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyTenderProfile("code-553", "label-650")
        item.status = LegacyTenderProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyTenderProfileStatus.PLANNED) }
    }
}
