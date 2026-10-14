package com.acme.transport.emissions.evcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalEvChargeTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalEvChargeTemplate("code-690", "label-660")
        assertEquals(RegionalEvChargeTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newRegionalEvChargeTemplate("code-690", "label-660")
        try {
            item.moveTo(RegionalEvChargeTemplateStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalEvChargeTemplateStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalEvChargeTemplate("code-690", "label-660")
        item.status = RegionalEvChargeTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalEvChargeTemplateStatus.DRAFT) }
    }
}
