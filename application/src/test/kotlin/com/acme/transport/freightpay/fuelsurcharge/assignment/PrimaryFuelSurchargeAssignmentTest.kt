package com.acme.transport.freightpay.fuelsurcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryFuelSurchargeAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryFuelSurchargeAssignment("code-823", "label-122")
        assertEquals(PrimaryFuelSurchargeAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPrimaryFuelSurchargeAssignment("code-823", "label-122")
        try {
            item.moveTo(PrimaryFuelSurchargeAssignmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryFuelSurchargeAssignmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryFuelSurchargeAssignment("code-823", "label-122")
        item.status = PrimaryFuelSurchargeAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryFuelSurchargeAssignmentStatus.REQUESTED) }
    }
}
