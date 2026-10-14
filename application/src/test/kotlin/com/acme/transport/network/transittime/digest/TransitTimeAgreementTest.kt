package com.acme.transport.network.transittime.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeAgreementTest {
    @Test
    fun `starts in requested`() {
        val item = newTransitTimeAgreement("code-107", "label-301")
        assertEquals(TransitTimeAgreementStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newTransitTimeAgreement("code-107", "label-301")
        try {
            item.moveTo(TransitTimeAgreementStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeAgreementStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTransitTimeAgreement("code-107", "label-301")
        item.status = TransitTimeAgreementStatus.EXPIRED
        assertFails("move") { item.moveTo(TransitTimeAgreementStatus.REQUESTED) }
    }
}
