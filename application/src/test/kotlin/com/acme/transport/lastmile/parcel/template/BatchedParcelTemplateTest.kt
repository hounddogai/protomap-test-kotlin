package com.acme.transport.lastmile.parcel.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedParcelTemplateTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedParcelTemplate("code-457", "label-672")
        assertEquals(BatchedParcelTemplateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBatchedParcelTemplate("code-457", "label-672")
        try {
            item.moveTo(BatchedParcelTemplateStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedParcelTemplateStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedParcelTemplate("code-457", "label-672")
        item.status = BatchedParcelTemplateStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedParcelTemplateStatus.REQUESTED) }
    }
}
