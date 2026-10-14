package com.acme.transport.fleetops.defect.defectsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDefectSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultDefectSummary("code-860", "label-927")
        assertEquals(DefaultDefectSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newDefaultDefectSummary("code-860", "label-927")
        try {
            item.moveTo(DefaultDefectSummaryStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDefectSummaryStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultDefectSummary("code-860", "label-927")
        item.status = DefaultDefectSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultDefectSummaryStatus.OPEN) }
    }
}
