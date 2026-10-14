package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedLanePlanVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedLanePlanVariance("code-582", "label-135")
        assertEquals(BatchedLanePlanVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newBatchedLanePlanVariance("code-582", "label-135")
        try {
            item.moveTo(BatchedLanePlanVarianceStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedLanePlanVarianceStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedLanePlanVariance("code-582", "label-135")
        item.status = BatchedLanePlanVarianceStatus.FAILED
        assertFails("move") { item.moveTo(BatchedLanePlanVarianceStatus.NEW) }
    }
}
