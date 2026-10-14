package com.acme.transport.lastmile.locker.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalLockerBatchTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalLockerBatch("code-703", "name-117")
        assertEquals(GlobalLockerBatchStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newGlobalLockerBatch("code-703", "name-117")
        try {
            item.moveTo(GlobalLockerBatchStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalLockerBatchStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalLockerBatch("code-703", "name-117")
        item.status = GlobalLockerBatchStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalLockerBatchStatus.CREATED) }
    }
}
