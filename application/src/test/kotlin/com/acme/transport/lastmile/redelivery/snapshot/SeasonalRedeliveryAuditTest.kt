package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRedeliveryAuditTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalRedeliveryAudit("code-224", "label-403")
        assertEquals(SeasonalRedeliveryAuditStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalRedeliveryAudit("code-224", "label-403")
        try {
            item.moveTo(SeasonalRedeliveryAuditStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRedeliveryAuditStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalRedeliveryAudit("code-224", "label-403")
        item.status = SeasonalRedeliveryAuditStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalRedeliveryAuditStatus.REQUESTED) }
    }
}
