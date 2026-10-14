package com.acme.transport.freightpay.fuelsurcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargePlanTest {
    @Test
    fun `starts in open`() {
        val item = newFuelSurchargePlan("code-908", "name-676")
        assertEquals(FuelSurchargePlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newFuelSurchargePlan("code-908", "name-676")
        try {
            item.moveTo(FuelSurchargePlanStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargePlanStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFuelSurchargePlan("code-908", "name-676")
        item.status = FuelSurchargePlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(FuelSurchargePlanStatus.OPEN) }
    }
}
