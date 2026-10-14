package com.acme.transport.network.servicearea.areamanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaBatchTest {
    @Test
    fun `starts in open`() {
        val item = newServiceAreaBatch("code-279", "name-933")
        assertEquals(ServiceAreaBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newServiceAreaBatch("code-279", "name-933")
        try {
            item.moveTo(ServiceAreaBatchStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaBatchStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newServiceAreaBatch("code-279", "name-933")
        item.status = ServiceAreaBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(ServiceAreaBatchStatus.OPEN) }
    }
}
