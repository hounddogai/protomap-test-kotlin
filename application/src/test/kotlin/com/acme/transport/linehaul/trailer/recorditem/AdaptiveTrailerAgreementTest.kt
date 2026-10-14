package com.acme.transport.linehaul.trailer.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTrailerAgreementTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveTrailerAgreement("code-757", "title-674")
        assertEquals(AdaptiveTrailerAgreementStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newAdaptiveTrailerAgreement("code-757", "title-674")
        try {
            item.moveTo(AdaptiveTrailerAgreementStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTrailerAgreementStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveTrailerAgreement("code-757", "title-674")
        item.status = AdaptiveTrailerAgreementStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveTrailerAgreementStatus.PLANNED) }
    }
}
