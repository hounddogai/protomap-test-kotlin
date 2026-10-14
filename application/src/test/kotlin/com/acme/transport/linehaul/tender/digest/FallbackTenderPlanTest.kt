package com.acme.transport.linehaul.tender.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTenderPlanTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackTenderPlan("code-806", "name-239")
        assertEquals(FallbackTenderPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFallbackTenderPlan("code-806", "name-239")
        try {
            item.moveTo(FallbackTenderPlanStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTenderPlanStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackTenderPlan("code-806", "name-239")
        item.status = FallbackTenderPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackTenderPlanStatus.OPEN) }
    }
}
