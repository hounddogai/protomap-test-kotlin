package com.acme.transport.linehaul.backhaul.backhaulallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedBackhaulAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedBackhaulAudit("code-447", "name-237")
        assertEquals(WeightedBackhaulAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newWeightedBackhaulAudit("code-447", "name-237")
        try {
            item.moveTo(WeightedBackhaulAuditStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedBackhaulAuditStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedBackhaulAudit("code-447", "name-237")
        item.status = WeightedBackhaulAuditStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedBackhaulAuditStatus.DRAFT) }
    }
}
