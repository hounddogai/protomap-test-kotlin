package com.acme.transport.linehaul.laneplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedLanePlanBundleTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedLanePlanBundle("code-453", "title-450")
        assertEquals(BatchedLanePlanBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newBatchedLanePlanBundle("code-453", "title-450")
        try {
            item.moveTo(BatchedLanePlanBundleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedLanePlanBundleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedLanePlanBundle("code-453", "title-450")
        item.status = BatchedLanePlanBundleStatus.FAILED
        assertFails("move") { item.moveTo(BatchedLanePlanBundleStatus.NEW) }
    }
}
