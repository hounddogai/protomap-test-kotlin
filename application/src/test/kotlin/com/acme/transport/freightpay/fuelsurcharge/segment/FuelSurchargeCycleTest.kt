package com.acme.transport.freightpay.fuelsurcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeCycleTest {
    @Test
    fun `starts in open`() {
        val item = newFuelSurchargeCycle("code-163", "name-657")
        assertEquals(FuelSurchargeCycleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newFuelSurchargeCycle("code-163", "name-657")
        try {
            item.moveTo(FuelSurchargeCycleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeCycleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFuelSurchargeCycle("code-163", "name-657")
        item.status = FuelSurchargeCycleStatus.ARCHIVED
        assertFails("move") { item.moveTo(FuelSurchargeCycleStatus.OPEN) }
    }
}
