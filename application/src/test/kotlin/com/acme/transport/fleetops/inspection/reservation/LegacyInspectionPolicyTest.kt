package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyInspectionPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyInspectionPolicy("code-52", "name-504")
        assertEquals(LegacyInspectionPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLegacyInspectionPolicy("code-52", "name-504")
        try {
            item.moveTo(LegacyInspectionPolicyStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyInspectionPolicyStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyInspectionPolicy("code-52", "name-504")
        item.status = LegacyInspectionPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyInspectionPolicyStatus.PROPOSED) }
    }
}
