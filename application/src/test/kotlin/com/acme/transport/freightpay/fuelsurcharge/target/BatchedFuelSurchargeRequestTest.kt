package com.acme.transport.freightpay.fuelsurcharge.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedFuelSurchargeRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedFuelSurchargeRequest("code-984", "title-349")
        assertEquals(BatchedFuelSurchargeRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBatchedFuelSurchargeRequest("code-984", "title-349")
        try {
            item.moveTo(BatchedFuelSurchargeRequestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedFuelSurchargeRequestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedFuelSurchargeRequest("code-984", "title-349")
        item.status = BatchedFuelSurchargeRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedFuelSurchargeRequestStatus.REQUESTED) }
    }
}
