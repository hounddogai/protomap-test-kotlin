package com.acme.transport.crewing.qualification.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedQualificationCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedQualificationCycle("code-337", "title-903")
        assertEquals(BatchedQualificationCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedQualificationCycle("code-337", "title-903")
        try {
            item.moveTo(BatchedQualificationCycleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedQualificationCycleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedQualificationCycle("code-337", "title-903")
        item.status = BatchedQualificationCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedQualificationCycleStatus.PROPOSED) }
    }
}
