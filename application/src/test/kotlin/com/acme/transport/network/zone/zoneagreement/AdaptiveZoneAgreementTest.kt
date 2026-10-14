package com.acme.transport.network.zone.zoneagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveZoneAgreementTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveZoneAgreement("code-849", "label-419")
        assertEquals(AdaptiveZoneAgreementStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newAdaptiveZoneAgreement("code-849", "label-419")
        try {
            item.moveTo(AdaptiveZoneAgreementStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveZoneAgreementStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveZoneAgreement("code-849", "label-419")
        item.status = AdaptiveZoneAgreementStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveZoneAgreementStatus.OPEN) }
    }
}
