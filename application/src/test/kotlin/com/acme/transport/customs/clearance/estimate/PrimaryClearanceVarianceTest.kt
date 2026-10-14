package com.acme.transport.customs.clearance.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryClearanceVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryClearanceVariance("code-914", "title-14")
        assertEquals(PrimaryClearanceVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newPrimaryClearanceVariance("code-914", "title-14")
        try {
            item.moveTo(PrimaryClearanceVarianceStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryClearanceVarianceStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryClearanceVariance("code-914", "title-14")
        item.status = PrimaryClearanceVarianceStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryClearanceVarianceStatus.NEW) }
    }
}
