package com.acme.transport.linehaul.tender.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTenderPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyTenderPreference("code-561", "label-819")
        assertEquals(LegacyTenderPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLegacyTenderPreference("code-561", "label-819")
        try {
            item.moveTo(LegacyTenderPreferenceStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTenderPreferenceStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyTenderPreference("code-561", "label-819")
        item.status = LegacyTenderPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(LegacyTenderPreferenceStatus.NEW) }
    }
}
