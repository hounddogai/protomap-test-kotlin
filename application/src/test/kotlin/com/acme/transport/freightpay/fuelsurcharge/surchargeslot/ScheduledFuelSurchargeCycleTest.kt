package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledFuelSurchargeCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledFuelSurchargeCycle("code-31", "label-83")
        assertEquals(ScheduledFuelSurchargeCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newScheduledFuelSurchargeCycle("code-31", "label-83")
        try {
            item.moveTo(ScheduledFuelSurchargeCycleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledFuelSurchargeCycleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledFuelSurchargeCycle("code-31", "label-83")
        item.status = ScheduledFuelSurchargeCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledFuelSurchargeCycleStatus.PROPOSED) }
    }
}
