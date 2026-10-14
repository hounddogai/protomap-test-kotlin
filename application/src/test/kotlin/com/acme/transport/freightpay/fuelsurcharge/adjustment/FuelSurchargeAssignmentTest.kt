package com.acme.transport.freightpay.fuelsurcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeAssignmentTest {
    @Test
    fun `starts in open`() {
        val item = newFuelSurchargeAssignment("code-578", "name-359")
        assertEquals(FuelSurchargeAssignmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFuelSurchargeAssignment("code-578", "name-359")
        try {
            item.moveTo(FuelSurchargeAssignmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeAssignmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFuelSurchargeAssignment("code-578", "name-359")
        item.status = FuelSurchargeAssignmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(FuelSurchargeAssignmentStatus.OPEN) }
    }
}
