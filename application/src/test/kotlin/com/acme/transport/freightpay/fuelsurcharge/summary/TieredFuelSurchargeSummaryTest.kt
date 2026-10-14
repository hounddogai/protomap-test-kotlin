package com.acme.transport.freightpay.fuelsurcharge.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredFuelSurchargeSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newTieredFuelSurchargeSummary("code-146", "title-273")
        assertEquals(TieredFuelSurchargeSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTieredFuelSurchargeSummary("code-146", "title-273")
        try {
            item.moveTo(TieredFuelSurchargeSummaryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredFuelSurchargeSummaryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredFuelSurchargeSummary("code-146", "title-273")
        item.status = TieredFuelSurchargeSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(TieredFuelSurchargeSummaryStatus.CREATED) }
    }
}
