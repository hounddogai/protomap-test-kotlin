package com.acme.transport.fleetops.inspection.inspectionagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedInspectionExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedInspectionException("code-675", "label-194")
        assertEquals(WeightedInspectionExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newWeightedInspectionException("code-675", "label-194")
        try {
            item.moveTo(WeightedInspectionExceptionStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedInspectionExceptionStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedInspectionException("code-675", "label-194")
        item.status = WeightedInspectionExceptionStatus.FAILED
        assertFails("move") { item.moveTo(WeightedInspectionExceptionStatus.NEW) }
    }
}
