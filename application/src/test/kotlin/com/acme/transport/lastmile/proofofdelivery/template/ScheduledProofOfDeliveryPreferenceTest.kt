package com.acme.transport.lastmile.proofofdelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledProofOfDeliveryPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledProofOfDeliveryPreference("code-234", "name-434")
        assertEquals(ScheduledProofOfDeliveryPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newScheduledProofOfDeliveryPreference("code-234", "name-434")
        try {
            item.moveTo(ScheduledProofOfDeliveryPreferenceStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledProofOfDeliveryPreferenceStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledProofOfDeliveryPreference("code-234", "name-434")
        item.status = ScheduledProofOfDeliveryPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledProofOfDeliveryPreferenceStatus.NEW) }
    }
}
