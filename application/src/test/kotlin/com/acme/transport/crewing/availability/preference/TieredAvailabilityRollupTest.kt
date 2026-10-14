package com.acme.transport.crewing.availability.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredAvailabilityRollupTest {
    @Test
    fun `starts in open`() {
        val item = newTieredAvailabilityRollup("code-710", "name-605")
        assertEquals(TieredAvailabilityRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newTieredAvailabilityRollup("code-710", "name-605")
        try {
            item.moveTo(TieredAvailabilityRollupStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredAvailabilityRollupStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredAvailabilityRollup("code-710", "name-605")
        item.status = TieredAvailabilityRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredAvailabilityRollupStatus.OPEN) }
    }
}
