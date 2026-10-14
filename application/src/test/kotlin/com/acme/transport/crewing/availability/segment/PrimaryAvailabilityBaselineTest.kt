package com.acme.transport.crewing.availability.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryAvailabilityBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryAvailabilityBaseline("code-416", "name-199")
        assertEquals(PrimaryAvailabilityBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPrimaryAvailabilityBaseline("code-416", "name-199")
        try {
            item.moveTo(PrimaryAvailabilityBaselineStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryAvailabilityBaselineStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryAvailabilityBaseline("code-416", "name-199")
        item.status = PrimaryAvailabilityBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryAvailabilityBaselineStatus.OPEN) }
    }
}
