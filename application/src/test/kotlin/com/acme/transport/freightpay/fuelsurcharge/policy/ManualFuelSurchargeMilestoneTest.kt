package com.acme.transport.freightpay.fuelsurcharge.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualFuelSurchargeMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newManualFuelSurchargeMilestone("code-934", "title-933")
        assertEquals(ManualFuelSurchargeMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newManualFuelSurchargeMilestone("code-934", "title-933")
        try {
            item.moveTo(ManualFuelSurchargeMilestoneStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualFuelSurchargeMilestoneStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualFuelSurchargeMilestone("code-934", "title-933")
        item.status = ManualFuelSurchargeMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(ManualFuelSurchargeMilestoneStatus.NEW) }
    }
}
