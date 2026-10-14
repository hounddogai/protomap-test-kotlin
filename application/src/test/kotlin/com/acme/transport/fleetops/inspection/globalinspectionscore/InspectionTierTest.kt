package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionTierTest {
    @Test
    fun `starts in requested`() {
        val item = newInspectionTier("code-38", "label-529")
        assertEquals(InspectionTierStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newInspectionTier("code-38", "label-529")
        try {
            item.moveTo(InspectionTierStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionTierStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newInspectionTier("code-38", "label-529")
        item.status = InspectionTierStatus.EXPIRED
        assertFails("move") { item.moveTo(InspectionTierStatus.REQUESTED) }
    }
}
