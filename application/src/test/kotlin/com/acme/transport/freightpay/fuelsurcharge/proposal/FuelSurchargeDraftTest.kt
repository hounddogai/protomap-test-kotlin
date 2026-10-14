package com.acme.transport.freightpay.fuelsurcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newFuelSurchargeDraft("code-740", "label-143")
        assertEquals(FuelSurchargeDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFuelSurchargeDraft("code-740", "label-143")
        try {
            item.moveTo(FuelSurchargeDraftStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeDraftStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFuelSurchargeDraft("code-740", "label-143")
        item.status = FuelSurchargeDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(FuelSurchargeDraftStatus.REQUESTED) }
    }
}
