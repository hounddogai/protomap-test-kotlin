package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newDisputeVariance("code-617", "name-670")
        assertEquals(DisputeVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDisputeVariance("code-617", "name-670")
        try {
            item.moveTo(DisputeVarianceStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeVarianceStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDisputeVariance("code-617", "name-670")
        item.status = DisputeVarianceStatus.FAILED
        assertFails("move") { item.moveTo(DisputeVarianceStatus.NEW) }
    }
}
