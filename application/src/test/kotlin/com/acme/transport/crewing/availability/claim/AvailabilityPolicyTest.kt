package com.acme.transport.crewing.availability.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newAvailabilityPolicy("code-248", "title-644")
        assertEquals(AvailabilityPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newAvailabilityPolicy("code-248", "title-644")
        try {
            item.moveTo(AvailabilityPolicyStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityPolicyStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAvailabilityPolicy("code-248", "title-644")
        item.status = AvailabilityPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(AvailabilityPolicyStatus.REQUESTED) }
    }
}
