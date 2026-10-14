package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledProofOfDeliveryMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledProofOfDeliveryMapping("code-118", "name-877")
        assertEquals(ScheduledProofOfDeliveryMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newScheduledProofOfDeliveryMapping("code-118", "name-877")
        try {
            item.moveTo(ScheduledProofOfDeliveryMappingStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledProofOfDeliveryMappingStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledProofOfDeliveryMapping("code-118", "name-877")
        item.status = ScheduledProofOfDeliveryMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledProofOfDeliveryMappingStatus.REQUESTED) }
    }
}
