package com.acme.transport.crewing.availability.availabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyAvailabilityDigestTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyAvailabilityDigest("code-150", "name-223")
        assertEquals(LegacyAvailabilityDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newLegacyAvailabilityDigest("code-150", "name-223")
        try {
            item.moveTo(LegacyAvailabilityDigestStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyAvailabilityDigestStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyAvailabilityDigest("code-150", "name-223")
        item.status = LegacyAvailabilityDigestStatus.FAILED
        assertFails("move") { item.moveTo(LegacyAvailabilityDigestStatus.NEW) }
    }
}
