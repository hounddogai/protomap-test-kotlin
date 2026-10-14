package com.acme.transport.fleetops.serviceplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedServicePlanGroupTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedServicePlanGroup("code-958", "name-201")
        assertEquals(BatchedServicePlanGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBatchedServicePlanGroup("code-958", "name-201")
        try {
            item.moveTo(BatchedServicePlanGroupStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedServicePlanGroupStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedServicePlanGroup("code-958", "name-201")
        item.status = BatchedServicePlanGroupStatus.FAILED
        assertFails("move") { item.moveTo(BatchedServicePlanGroupStatus.NEW) }
    }
}
