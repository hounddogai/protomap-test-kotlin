package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeRuleTest {
    @Test
    fun `starts in open`() {
        val item = newTransitTimeRule("code-370", "name-564")
        assertEquals(TransitTimeRuleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newTransitTimeRule("code-370", "name-564")
        try {
            item.moveTo(TransitTimeRuleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeRuleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTransitTimeRule("code-370", "name-564")
        item.status = TransitTimeRuleStatus.ARCHIVED
        assertFails("move") { item.moveTo(TransitTimeRuleStatus.OPEN) }
    }
}
