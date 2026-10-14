package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryFuelSurchargeTierTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryFuelSurchargeTier("code-350", "label-641")
        assertEquals(PrimaryFuelSurchargeTierStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newPrimaryFuelSurchargeTier("code-350", "label-641")
        try {
            item.moveTo(PrimaryFuelSurchargeTierStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryFuelSurchargeTierStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryFuelSurchargeTier("code-350", "label-641")
        item.status = PrimaryFuelSurchargeTierStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryFuelSurchargeTierStatus.IDLE) }
    }
}
