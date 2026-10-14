package com.acme.transport.freightpay.dispute.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDisputeTemplateTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyDisputeTemplate("code-553", "label-755")
        assertEquals(LegacyDisputeTemplateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLegacyDisputeTemplate("code-553", "label-755")
        try {
            item.moveTo(LegacyDisputeTemplateStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDisputeTemplateStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyDisputeTemplate("code-553", "label-755")
        item.status = LegacyDisputeTemplateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyDisputeTemplateStatus.PROPOSED) }
    }
}
