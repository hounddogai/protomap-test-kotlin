package com.acme.transport.lastmile.redelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRedeliveryQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictRedeliveryQuota("code-562", "title-18")
        assertEquals(StrictRedeliveryQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newStrictRedeliveryQuota("code-562", "title-18")
        try {
            item.moveTo(StrictRedeliveryQuotaStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRedeliveryQuotaStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictRedeliveryQuota("code-562", "title-18")
        item.status = StrictRedeliveryQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(StrictRedeliveryQuotaStatus.IDLE) }
    }
}
