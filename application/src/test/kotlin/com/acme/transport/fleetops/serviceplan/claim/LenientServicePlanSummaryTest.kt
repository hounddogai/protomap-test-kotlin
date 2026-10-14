package com.acme.transport.fleetops.serviceplan.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientServicePlanSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientServicePlanSummary("code-709", "name-22")
        assertEquals(LenientServicePlanSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newLenientServicePlanSummary("code-709", "name-22")
        try {
            item.moveTo(LenientServicePlanSummaryStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientServicePlanSummaryStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientServicePlanSummary("code-709", "name-22")
        item.status = LenientServicePlanSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientServicePlanSummaryStatus.PLANNED) }
    }
}
