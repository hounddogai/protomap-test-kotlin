package com.acme.transport.fleetops.utilization.utilizationreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationBatchTest {
    @Test
    fun `starts in new`() {
        val item = newUtilizationBatch("code-491", "title-219")
        assertEquals(UtilizationBatchStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newUtilizationBatch("code-491", "title-219")
        try {
            item.moveTo(UtilizationBatchStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationBatchStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newUtilizationBatch("code-491", "title-219")
        item.status = UtilizationBatchStatus.FAILED
        assertFails("move") { item.moveTo(UtilizationBatchStatus.NEW) }
    }
}
