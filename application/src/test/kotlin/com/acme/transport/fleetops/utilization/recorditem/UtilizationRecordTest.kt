package com.acme.transport.fleetops.utilization.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationRecordTest {
    @Test
    fun `starts in new`() {
        val item = newUtilizationRecord("code-414", "label-759")
        assertEquals(UtilizationRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newUtilizationRecord("code-414", "label-759")
        try {
            item.moveTo(UtilizationRecordStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationRecordStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newUtilizationRecord("code-414", "label-759")
        item.status = UtilizationRecordStatus.FAILED
        assertFails("move") { item.moveTo(UtilizationRecordStatus.NEW) }
    }
}
