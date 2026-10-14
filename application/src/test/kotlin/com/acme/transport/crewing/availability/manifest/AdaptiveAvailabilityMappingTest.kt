package com.acme.transport.crewing.availability.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAvailabilityMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveAvailabilityMapping("code-373", "title-655")
        assertEquals(AdaptiveAvailabilityMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAdaptiveAvailabilityMapping("code-373", "title-655")
        try {
            item.moveTo(AdaptiveAvailabilityMappingStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAvailabilityMappingStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveAvailabilityMapping("code-373", "title-655")
        item.status = AdaptiveAvailabilityMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveAvailabilityMappingStatus.REQUESTED) }
    }
}
