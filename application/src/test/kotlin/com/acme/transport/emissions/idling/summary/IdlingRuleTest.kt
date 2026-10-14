package com.acme.transport.emissions.idling.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newIdlingRule("code-538", "title-66")
        assertEquals(IdlingRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newIdlingRule("code-538", "title-66")
        try {
            item.moveTo(IdlingRuleStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingRuleStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newIdlingRule("code-538", "title-66")
        item.status = IdlingRuleStatus.STOPPED
        assertFails("move") { item.moveTo(IdlingRuleStatus.IDLE) }
    }
}
