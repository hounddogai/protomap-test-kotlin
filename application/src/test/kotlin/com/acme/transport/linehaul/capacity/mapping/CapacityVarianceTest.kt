package com.acme.transport.linehaul.capacity.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newCapacityVariance("code-863", "label-375")
        assertEquals(CapacityVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newCapacityVariance("code-863", "label-375")
        try {
            item.moveTo(CapacityVarianceStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityVarianceStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCapacityVariance("code-863", "label-375")
        item.status = CapacityVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CapacityVarianceStatus.PROPOSED) }
    }
}
