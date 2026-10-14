package com.acme.transport.linehaul.tender.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newTenderPreference("code-672", "label-819")
        assertEquals(TenderPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newTenderPreference("code-672", "label-819")
        try {
            item.moveTo(TenderPreferenceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderPreferenceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTenderPreference("code-672", "label-819")
        item.status = TenderPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(TenderPreferenceStatus.PLANNED) }
    }
}
