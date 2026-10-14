package com.acme.transport.freightpay.fuelsurcharge.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargePolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newFuelSurchargePolicy("code-745", "title-470")
        assertEquals(FuelSurchargePolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newFuelSurchargePolicy("code-745", "title-470")
        try {
            item.moveTo(FuelSurchargePolicyStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargePolicyStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFuelSurchargePolicy("code-745", "title-470")
        item.status = FuelSurchargePolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FuelSurchargePolicyStatus.PROPOSED) }
    }
}
