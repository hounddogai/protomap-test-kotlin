package com.acme.transport.crewing.qualification.manualqualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingQualificationPreferenceTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingQualificationPreference("code-883", "name-994")
        assertEquals(PendingQualificationPreferenceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPendingQualificationPreference("code-883", "name-994")
        try {
            item.moveTo(PendingQualificationPreferenceStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingQualificationPreferenceStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingQualificationPreference("code-883", "name-994")
        item.status = PendingQualificationPreferenceStatus.CLOSED
        assertFails("move") { item.moveTo(PendingQualificationPreferenceStatus.DRAFT) }
    }
}
