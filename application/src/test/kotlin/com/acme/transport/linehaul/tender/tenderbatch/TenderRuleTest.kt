package com.acme.transport.linehaul.tender.tenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newTenderRule("code-493", "label-445")
        assertEquals(TenderRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTenderRule("code-493", "label-445")
        try {
            item.moveTo(TenderRuleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderRuleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTenderRule("code-493", "label-445")
        item.status = TenderRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(TenderRuleStatus.REQUESTED) }
    }
}
