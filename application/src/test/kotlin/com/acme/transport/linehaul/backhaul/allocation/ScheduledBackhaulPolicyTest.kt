package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledBackhaulPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledBackhaulPolicy("code-412", "title-280")
        assertEquals(ScheduledBackhaulPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newScheduledBackhaulPolicy("code-412", "title-280")
        try {
            item.moveTo(ScheduledBackhaulPolicyStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledBackhaulPolicyStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledBackhaulPolicy("code-412", "title-280")
        item.status = ScheduledBackhaulPolicyStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledBackhaulPolicyStatus.NEW) }
    }
}
