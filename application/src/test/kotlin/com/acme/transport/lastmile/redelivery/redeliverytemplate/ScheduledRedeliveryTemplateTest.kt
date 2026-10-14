package com.acme.transport.lastmile.redelivery.redeliverytemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRedeliveryTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledRedeliveryTemplate("code-812", "name-820")
        assertEquals(ScheduledRedeliveryTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newScheduledRedeliveryTemplate("code-812", "name-820")
        try {
            item.moveTo(ScheduledRedeliveryTemplateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRedeliveryTemplateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledRedeliveryTemplate("code-812", "name-820")
        item.status = ScheduledRedeliveryTemplateStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledRedeliveryTemplateStatus.NEW) }
    }
}
