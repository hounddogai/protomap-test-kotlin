package com.acme.transport.lastmile.redelivery.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRedeliveryTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingRedeliveryTemplate("code-505", "label-743")
        assertEquals(RollingRedeliveryTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRollingRedeliveryTemplate("code-505", "label-743")
        try {
            item.moveTo(RollingRedeliveryTemplateStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRedeliveryTemplateStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingRedeliveryTemplate("code-505", "label-743")
        item.status = RollingRedeliveryTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(RollingRedeliveryTemplateStatus.DRAFT) }
    }
}
