package com.acme.transport.fleetops.defect.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectTierTest {
    @Test
    fun `starts in draft`() {
        val item = newDefectTier("code-648", "name-597")
        assertEquals(DefectTierStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDefectTier("code-648", "name-597")
        try {
            item.moveTo(DefectTierStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectTierStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefectTier("code-648", "name-597")
        item.status = DefectTierStatus.CLOSED
        assertFails("move") { item.moveTo(DefectTierStatus.DRAFT) }
    }
}
