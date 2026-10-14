package com.acme.transport.lastmile.redelivery.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newRedeliveryThreshold("code-133", "name-874")
        assertEquals(RedeliveryThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRedeliveryThreshold("code-133", "name-874")
        try {
            item.moveTo(RedeliveryThresholdStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryThresholdStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRedeliveryThreshold("code-133", "name-874")
        item.status = RedeliveryThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RedeliveryThresholdStatus.PROPOSED) }
    }
}
