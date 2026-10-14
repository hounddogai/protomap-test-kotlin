package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledZoneTemplateTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledZoneTemplate("code-878", "label-44")
        assertEquals(ScheduledZoneTemplateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newScheduledZoneTemplate("code-878", "label-44")
        try {
            item.moveTo(ScheduledZoneTemplateStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledZoneTemplateStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledZoneTemplate("code-878", "label-44")
        item.status = ScheduledZoneTemplateStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledZoneTemplateStatus.IDLE) }
    }
}
