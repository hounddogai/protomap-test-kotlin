package com.acme.transport.network.transittime.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeBatchTest {
    @Test
    fun `starts in new`() {
        val item = newTransitTimeBatch("code-436", "name-705")
        assertEquals(TransitTimeBatchStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newTransitTimeBatch("code-436", "name-705")
        try {
            item.moveTo(TransitTimeBatchStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeBatchStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTransitTimeBatch("code-436", "name-705")
        item.status = TransitTimeBatchStatus.FAILED
        assertFails("move") { item.moveTo(TransitTimeBatchStatus.NEW) }
    }
}
