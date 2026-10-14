package com.acme.transport.linehaul.tender.tenderrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTenderVarianceTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyTenderVariance("code-828", "label-891")
        assertEquals(LegacyTenderVarianceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLegacyTenderVariance("code-828", "label-891")
        try {
            item.moveTo(LegacyTenderVarianceStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTenderVarianceStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyTenderVariance("code-828", "label-891")
        item.status = LegacyTenderVarianceStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyTenderVarianceStatus.DRAFT) }
    }
}
