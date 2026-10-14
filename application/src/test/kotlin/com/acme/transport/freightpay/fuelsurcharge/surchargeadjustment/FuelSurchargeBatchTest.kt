package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeBatchTest {
    @Test
    fun `starts in open`() {
        val item = newFuelSurchargeBatch("code-36", "name-704")
        assertEquals(FuelSurchargeBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newFuelSurchargeBatch("code-36", "name-704")
        try {
            item.moveTo(FuelSurchargeBatchStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeBatchStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFuelSurchargeBatch("code-36", "name-704")
        item.status = FuelSurchargeBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(FuelSurchargeBatchStatus.OPEN) }
    }
}
