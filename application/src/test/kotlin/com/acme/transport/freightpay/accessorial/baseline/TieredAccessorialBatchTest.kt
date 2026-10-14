package com.acme.transport.freightpay.accessorial.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredAccessorialBatchTest {
    @Test
    fun `starts in open`() {
        val item = newTieredAccessorialBatch("code-208", "title-229")
        assertEquals(TieredAccessorialBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newTieredAccessorialBatch("code-208", "title-229")
        try {
            item.moveTo(TieredAccessorialBatchStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredAccessorialBatchStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredAccessorialBatch("code-208", "title-229")
        item.status = TieredAccessorialBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredAccessorialBatchStatus.OPEN) }
    }
}
