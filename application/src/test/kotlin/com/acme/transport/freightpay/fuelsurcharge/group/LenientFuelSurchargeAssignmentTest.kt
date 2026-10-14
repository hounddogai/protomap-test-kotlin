package com.acme.transport.freightpay.fuelsurcharge.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientFuelSurchargeAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientFuelSurchargeAssignment("code-41", "label-39")
        assertEquals(LenientFuelSurchargeAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLenientFuelSurchargeAssignment("code-41", "label-39")
        try {
            item.moveTo(LenientFuelSurchargeAssignmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientFuelSurchargeAssignmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientFuelSurchargeAssignment("code-41", "label-39")
        item.status = LenientFuelSurchargeAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientFuelSurchargeAssignmentStatus.REQUESTED) }
    }
}
