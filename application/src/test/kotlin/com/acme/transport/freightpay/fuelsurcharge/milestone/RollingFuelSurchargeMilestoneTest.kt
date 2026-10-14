package com.acme.transport.freightpay.fuelsurcharge.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingFuelSurchargeMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newRollingFuelSurchargeMilestone("code-816", "name-345")
        assertEquals(RollingFuelSurchargeMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRollingFuelSurchargeMilestone("code-816", "name-345")
        try {
            item.moveTo(RollingFuelSurchargeMilestoneStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingFuelSurchargeMilestoneStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingFuelSurchargeMilestone("code-816", "name-345")
        item.status = RollingFuelSurchargeMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(RollingFuelSurchargeMilestoneStatus.NEW) }
    }
}
