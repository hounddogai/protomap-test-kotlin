package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionBundleTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveInspectionBundle("code-148", "name-831")
        assertEquals(AdaptiveInspectionBundleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAdaptiveInspectionBundle("code-148", "name-831")
        try {
            item.moveTo(AdaptiveInspectionBundleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionBundleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveInspectionBundle("code-148", "name-831")
        item.status = AdaptiveInspectionBundleStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveInspectionBundleStatus.CREATED) }
    }
}
