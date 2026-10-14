package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacitySummaryTest {
    @Test
    fun `starts in created`() {
        val item = newCapacitySummary("code-943", "name-274")
        assertEquals(CapacitySummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCapacitySummary("code-943", "name-274")
        try {
            item.moveTo(CapacitySummaryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacitySummaryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCapacitySummary("code-943", "name-274")
        item.status = CapacitySummaryStatus.RETIRED
        assertFails("move") { item.moveTo(CapacitySummaryStatus.CREATED) }
    }
}
