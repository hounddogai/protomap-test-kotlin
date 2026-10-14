package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTenderAgreementTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveTenderAgreement("code-11", "title-843")
        assertEquals(AdaptiveTenderAgreementStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAdaptiveTenderAgreement("code-11", "title-843")
        try {
            item.moveTo(AdaptiveTenderAgreementStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTenderAgreementStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveTenderAgreement("code-11", "title-843")
        item.status = AdaptiveTenderAgreementStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveTenderAgreementStatus.REQUESTED) }
    }
}
