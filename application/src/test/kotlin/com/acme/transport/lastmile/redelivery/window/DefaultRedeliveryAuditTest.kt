package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRedeliveryAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultRedeliveryAudit("code-808", "label-404")
        assertEquals(DefaultRedeliveryAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDefaultRedeliveryAudit("code-808", "label-404")
        try {
            item.moveTo(DefaultRedeliveryAuditStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRedeliveryAuditStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultRedeliveryAudit("code-808", "label-404")
        item.status = DefaultRedeliveryAuditStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultRedeliveryAuditStatus.DRAFT) }
    }
}
