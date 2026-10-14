package com.acme.transport.freightpay.fuelsurcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeGroupTest {
    @Test
    fun `starts in open`() {
        val item = newFuelSurchargeGroup("code-662", "title-884")
        assertEquals(FuelSurchargeGroupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newFuelSurchargeGroup("code-662", "title-884")
        try {
            item.moveTo(FuelSurchargeGroupStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeGroupStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFuelSurchargeGroup("code-662", "title-884")
        item.status = FuelSurchargeGroupStatus.ARCHIVED
        assertFails("move") { item.moveTo(FuelSurchargeGroupStatus.OPEN) }
    }
}
