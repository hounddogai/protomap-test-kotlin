package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTenderRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newManualTenderRule("code-33", "label-760")
        assertEquals(ManualTenderRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newManualTenderRule("code-33", "label-760")
        try {
            item.moveTo(ManualTenderRuleStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTenderRuleStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualTenderRule("code-33", "label-760")
        item.status = ManualTenderRuleStatus.STOPPED
        assertFails("move") { item.moveTo(ManualTenderRuleStatus.IDLE) }
    }
}
