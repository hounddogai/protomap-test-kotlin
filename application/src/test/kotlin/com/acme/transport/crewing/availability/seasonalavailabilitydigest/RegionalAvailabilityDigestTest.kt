package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalAvailabilityDigestTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalAvailabilityDigest("code-666", "label-638")
        assertEquals(RegionalAvailabilityDigestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newRegionalAvailabilityDigest("code-666", "label-638")
        try {
            item.moveTo(RegionalAvailabilityDigestStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalAvailabilityDigestStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalAvailabilityDigest("code-666", "label-638")
        item.status = RegionalAvailabilityDigestStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalAvailabilityDigestStatus.CREATED) }
    }
}
