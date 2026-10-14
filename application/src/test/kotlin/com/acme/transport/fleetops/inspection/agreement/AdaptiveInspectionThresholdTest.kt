package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveInspectionThreshold("code-281", "name-796")
        assertEquals(AdaptiveInspectionThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAdaptiveInspectionThreshold("code-281", "name-796")
        try {
            item.moveTo(AdaptiveInspectionThresholdStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionThresholdStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveInspectionThreshold("code-281", "name-796")
        item.status = AdaptiveInspectionThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveInspectionThresholdStatus.REQUESTED) }
    }
}
