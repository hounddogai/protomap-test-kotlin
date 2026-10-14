package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDisputeBundleTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledDisputeBundle("code-598", "label-578")
        assertEquals(ScheduledDisputeBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newScheduledDisputeBundle("code-598", "label-578")
        try {
            item.moveTo(ScheduledDisputeBundleStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDisputeBundleStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledDisputeBundle("code-598", "label-578")
        item.status = ScheduledDisputeBundleStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledDisputeBundleStatus.NEW) }
    }
}
