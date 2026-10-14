package com.acme.transport.crewing.availability.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAvailabilityManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveAvailabilityManifest("code-819", "label-909")
        assertEquals(AdaptiveAvailabilityManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAdaptiveAvailabilityManifest("code-819", "label-909")
        try {
            item.moveTo(AdaptiveAvailabilityManifestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAvailabilityManifestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveAvailabilityManifest("code-819", "label-909")
        item.status = AdaptiveAvailabilityManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveAvailabilityManifestStatus.PROPOSED) }
    }
}
