package com.acme.transport.network.servicearea.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newServiceAreaSummary("code-784", "name-989")
        assertEquals(ServiceAreaSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newServiceAreaSummary("code-784", "name-989")
        try {
            item.moveTo(ServiceAreaSummaryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaSummaryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newServiceAreaSummary("code-784", "name-989")
        item.status = ServiceAreaSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(ServiceAreaSummaryStatus.CREATED) }
    }
}
