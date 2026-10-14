package com.acme.transport.crewing.qualification.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedQualificationSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedQualificationSummary("code-811", "name-820")
        assertEquals(BatchedQualificationSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBatchedQualificationSummary("code-811", "name-820")
        try {
            item.moveTo(BatchedQualificationSummaryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedQualificationSummaryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedQualificationSummary("code-811", "name-820")
        item.status = BatchedQualificationSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedQualificationSummaryStatus.CREATED) }
    }
}
