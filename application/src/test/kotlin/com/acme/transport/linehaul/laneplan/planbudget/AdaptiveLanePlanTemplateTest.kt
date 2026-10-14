package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveLanePlanTemplateTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveLanePlanTemplate("code-653", "name-405")
        assertEquals(AdaptiveLanePlanTemplateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAdaptiveLanePlanTemplate("code-653", "name-405")
        try {
            item.moveTo(AdaptiveLanePlanTemplateStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveLanePlanTemplateStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveLanePlanTemplate("code-653", "name-405")
        item.status = AdaptiveLanePlanTemplateStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveLanePlanTemplateStatus.REQUESTED) }
    }
}
