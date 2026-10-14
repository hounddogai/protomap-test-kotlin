package com.acme.transport.network.zone.zonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientZoneAgreementTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientZoneAgreement("code-530", "label-18")
        assertEquals(LenientZoneAgreementStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newLenientZoneAgreement("code-530", "label-18")
        try {
            item.moveTo(LenientZoneAgreementStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientZoneAgreementStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientZoneAgreement("code-530", "label-18")
        item.status = LenientZoneAgreementStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientZoneAgreementStatus.REQUESTED) }
    }
}
