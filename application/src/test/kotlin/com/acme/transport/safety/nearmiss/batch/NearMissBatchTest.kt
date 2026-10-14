package com.acme.transport.safety.nearmiss.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissBatchTest {
    @Test
    fun `starts in open`() {
        val item = newNearMissBatch("code-713", "title-68")
        assertEquals(NearMissBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newNearMissBatch("code-713", "title-68")
        try {
            item.moveTo(NearMissBatchStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissBatchStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newNearMissBatch("code-713", "title-68")
        item.status = NearMissBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(NearMissBatchStatus.OPEN) }
    }
}
