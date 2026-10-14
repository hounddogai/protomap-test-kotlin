package com.acme.transport.emissions.idling.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualIdlingRuleTest {
    @Test
    fun `starts in draft`() {
        val item = newManualIdlingRule("code-100", "title-89")
        assertEquals(ManualIdlingRuleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newManualIdlingRule("code-100", "title-89")
        try {
            item.moveTo(ManualIdlingRuleStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualIdlingRuleStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualIdlingRule("code-100", "title-89")
        item.status = ManualIdlingRuleStatus.CLOSED
        assertFails("move") { item.moveTo(ManualIdlingRuleStatus.DRAFT) }
    }
}
