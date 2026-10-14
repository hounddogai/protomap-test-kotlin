package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackFuelSurchargeMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackFuelSurchargeMilestone("code-764", "title-52")
        assertEquals(FallbackFuelSurchargeMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newFallbackFuelSurchargeMilestone("code-764", "title-52")
        try {
            item.moveTo(FallbackFuelSurchargeMilestoneStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackFuelSurchargeMilestoneStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackFuelSurchargeMilestone("code-764", "title-52")
        item.status = FallbackFuelSurchargeMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackFuelSurchargeMilestoneStatus.OPEN) }
    }
}
