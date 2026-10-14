package com.acme.transport.lastmile.redelivery.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newRedeliveryVariance("code-631", "title-888")
        assertEquals(RedeliveryVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRedeliveryVariance("code-631", "title-888")
        try {
            item.moveTo(RedeliveryVarianceStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryVarianceStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRedeliveryVariance("code-631", "title-888")
        item.status = RedeliveryVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(RedeliveryVarianceStatus.IDLE) }
    }
}
