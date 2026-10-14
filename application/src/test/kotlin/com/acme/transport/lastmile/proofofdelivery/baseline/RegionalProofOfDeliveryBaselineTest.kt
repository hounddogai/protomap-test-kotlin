package com.acme.transport.lastmile.proofofdelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalProofOfDeliveryBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalProofOfDeliveryBaseline("code-14", "title-686")
        assertEquals(RegionalProofOfDeliveryBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalProofOfDeliveryBaseline("code-14", "title-686")
        try {
            item.moveTo(RegionalProofOfDeliveryBaselineStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalProofOfDeliveryBaselineStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalProofOfDeliveryBaseline("code-14", "title-686")
        item.status = RegionalProofOfDeliveryBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalProofOfDeliveryBaselineStatus.REQUESTED) }
    }
}
