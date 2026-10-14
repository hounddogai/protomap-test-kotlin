package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityProfileTest {
    @Test
    fun `starts in open`() {
        val item = newAvailabilityProfile("code-498", "title-822")
        assertEquals(AvailabilityProfileStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newAvailabilityProfile("code-498", "title-822")
        try {
            item.moveTo(AvailabilityProfileStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityProfileStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAvailabilityProfile("code-498", "title-822")
        item.status = AvailabilityProfileStatus.ARCHIVED
        assertFails("move") { item.moveTo(AvailabilityProfileStatus.OPEN) }
    }
}
