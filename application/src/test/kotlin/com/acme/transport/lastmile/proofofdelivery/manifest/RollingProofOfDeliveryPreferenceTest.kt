package com.acme.transport.lastmile.proofofdelivery.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingProofOfDeliveryPreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingProofOfDeliveryPreference("code-816", "name-369")
        assertEquals(RollingProofOfDeliveryPreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingProofOfDeliveryPreference("code-816", "name-369")
        try {
            item.moveTo(RollingProofOfDeliveryPreferenceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingProofOfDeliveryPreferenceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingProofOfDeliveryPreference("code-816", "name-369")
        item.status = RollingProofOfDeliveryPreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingProofOfDeliveryPreferenceStatus.PROPOSED) }
    }
}
