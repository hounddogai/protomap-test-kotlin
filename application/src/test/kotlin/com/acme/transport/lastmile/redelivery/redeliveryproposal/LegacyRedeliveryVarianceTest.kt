package com.acme.transport.lastmile.redelivery.redeliveryproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRedeliveryVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyRedeliveryVariance("code-223", "name-169")
        assertEquals(LegacyRedeliveryVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLegacyRedeliveryVariance("code-223", "name-169")
        try {
            item.moveTo(LegacyRedeliveryVarianceStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRedeliveryVarianceStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyRedeliveryVariance("code-223", "name-169")
        item.status = LegacyRedeliveryVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyRedeliveryVarianceStatus.IDLE) }
    }
}
