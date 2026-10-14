package com.acme.transport.fleetops.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveInspectionPolicy("code-693", "name-880")
        assertEquals(AdaptiveInspectionPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newAdaptiveInspectionPolicy("code-693", "name-880")
        try {
            item.moveTo(AdaptiveInspectionPolicyStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionPolicyStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveInspectionPolicy("code-693", "name-880")
        item.status = AdaptiveInspectionPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveInspectionPolicyStatus.PROPOSED) }
    }
}
