package com.acme.transport.fleetops.defect.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectBatchTest {
    @Test
    fun `starts in created`() {
        val item = newDefectBatch("code-762", "name-150")
        assertEquals(DefectBatchStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newDefectBatch("code-762", "name-150")
        try {
            item.moveTo(DefectBatchStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectBatchStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefectBatch("code-762", "name-150")
        item.status = DefectBatchStatus.RETIRED
        assertFails("move") { item.moveTo(DefectBatchStatus.CREATED) }
    }
}
