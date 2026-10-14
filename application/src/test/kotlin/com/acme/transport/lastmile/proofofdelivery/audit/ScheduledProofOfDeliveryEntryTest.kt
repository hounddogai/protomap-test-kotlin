package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledProofOfDeliveryEntryTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledProofOfDeliveryEntry("code-705", "name-481")
        assertEquals(ScheduledProofOfDeliveryEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledProofOfDeliveryEntry("code-705", "name-481")
        try {
            item.moveTo(ScheduledProofOfDeliveryEntryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledProofOfDeliveryEntryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledProofOfDeliveryEntry("code-705", "name-481")
        item.status = ScheduledProofOfDeliveryEntryStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledProofOfDeliveryEntryStatus.CREATED) }
    }
}
