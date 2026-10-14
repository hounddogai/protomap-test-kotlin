package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingProofOfDeliveryEntryTest {
    @Test
    fun `starts in open`() {
        val item = newRollingProofOfDeliveryEntry("code-578", "title-896")
        assertEquals(RollingProofOfDeliveryEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRollingProofOfDeliveryEntry("code-578", "title-896")
        try {
            item.moveTo(RollingProofOfDeliveryEntryStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingProofOfDeliveryEntryStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingProofOfDeliveryEntry("code-578", "title-896")
        item.status = RollingProofOfDeliveryEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingProofOfDeliveryEntryStatus.OPEN) }
    }
}
