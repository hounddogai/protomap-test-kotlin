package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalParcelBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalParcelBatch("code-363", "name-235")
        assertEquals(GlobalParcelBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newGlobalParcelBatch("code-363", "name-235")
        try {
            item.moveTo(GlobalParcelBatchStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalParcelBatchStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalParcelBatch("code-363", "name-235")
        item.status = GlobalParcelBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalParcelBatchStatus.REQUESTED) }
    }
}
