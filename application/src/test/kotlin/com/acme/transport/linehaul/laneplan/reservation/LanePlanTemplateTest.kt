package com.acme.transport.linehaul.laneplan.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newLanePlanTemplate("code-8", "label-893")
        assertEquals(LanePlanTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newLanePlanTemplate("code-8", "label-893")
        try {
            item.moveTo(LanePlanTemplateStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanTemplateStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLanePlanTemplate("code-8", "label-893")
        item.status = LanePlanTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(LanePlanTemplateStatus.PLANNED) }
    }
}
