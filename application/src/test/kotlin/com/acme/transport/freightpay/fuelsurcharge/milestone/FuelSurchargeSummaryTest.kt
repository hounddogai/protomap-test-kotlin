package com.acme.transport.freightpay.fuelsurcharge.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeSummaryTest {
    @Test
    fun `starts in draft`() {
        val item = newFuelSurchargeSummary("code-789", "label-892")
        assertEquals(FuelSurchargeSummaryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFuelSurchargeSummary("code-789", "label-892")
        try {
            item.moveTo(FuelSurchargeSummaryStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeSummaryStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFuelSurchargeSummary("code-789", "label-892")
        item.status = FuelSurchargeSummaryStatus.CLOSED
        assertFails("move") { item.moveTo(FuelSurchargeSummaryStatus.DRAFT) }
    }
}
