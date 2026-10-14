package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictFuelSurchargeSummaryTest {
    @Test
    fun `starts in new`() {
        val item = newStrictFuelSurchargeSummary("code-839", "title-919")
        assertEquals(StrictFuelSurchargeSummaryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newStrictFuelSurchargeSummary("code-839", "title-919")
        try {
            item.moveTo(StrictFuelSurchargeSummaryStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictFuelSurchargeSummaryStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictFuelSurchargeSummary("code-839", "title-919")
        item.status = StrictFuelSurchargeSummaryStatus.FAILED
        assertFails("move") { item.moveTo(StrictFuelSurchargeSummaryStatus.NEW) }
    }
}
