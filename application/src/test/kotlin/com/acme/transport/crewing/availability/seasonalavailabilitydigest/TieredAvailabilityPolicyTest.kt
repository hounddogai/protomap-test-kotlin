package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredAvailabilityPolicyTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredAvailabilityPolicy("code-937", "title-378")
        assertEquals(TieredAvailabilityPolicyStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTieredAvailabilityPolicy("code-937", "title-378")
        try {
            item.moveTo(TieredAvailabilityPolicyStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredAvailabilityPolicyStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredAvailabilityPolicy("code-937", "title-378")
        item.status = TieredAvailabilityPolicyStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredAvailabilityPolicyStatus.PLANNED) }
    }
}
