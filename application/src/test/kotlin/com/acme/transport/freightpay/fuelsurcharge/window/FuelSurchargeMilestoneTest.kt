package com.acme.transport.freightpay.fuelsurcharge.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newFuelSurchargeMilestone("code-604", "label-989")
        assertEquals(FuelSurchargeMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFuelSurchargeMilestone("code-604", "label-989")
        try {
            item.moveTo(FuelSurchargeMilestoneStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeMilestoneStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFuelSurchargeMilestone("code-604", "label-989")
        item.status = FuelSurchargeMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(FuelSurchargeMilestoneStatus.IDLE) }
    }
}
