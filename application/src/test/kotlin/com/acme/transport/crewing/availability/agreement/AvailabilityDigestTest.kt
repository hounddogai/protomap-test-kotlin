package com.acme.transport.crewing.availability.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityDigestTest {
    @Test
    fun `starts in draft`() {
        val item = newAvailabilityDigest("code-954", "label-379")
        assertEquals(AvailabilityDigestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newAvailabilityDigest("code-954", "label-379")
        try {
            item.moveTo(AvailabilityDigestStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityDigestStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAvailabilityDigest("code-954", "label-379")
        item.status = AvailabilityDigestStatus.CLOSED
        assertFails("move") { item.moveTo(AvailabilityDigestStatus.DRAFT) }
    }
}
