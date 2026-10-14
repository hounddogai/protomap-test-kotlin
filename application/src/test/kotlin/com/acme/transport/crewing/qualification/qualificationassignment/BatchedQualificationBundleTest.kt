package com.acme.transport.crewing.qualification.qualificationassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedQualificationBundleTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedQualificationBundle("code-704", "label-462")
        assertEquals(BatchedQualificationBundleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBatchedQualificationBundle("code-704", "label-462")
        try {
            item.moveTo(BatchedQualificationBundleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedQualificationBundleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedQualificationBundle("code-704", "label-462")
        item.status = BatchedQualificationBundleStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedQualificationBundleStatus.CREATED) }
    }
}
