package com.acme.transport.crewing.availability.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAvailabilityDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedAvailabilityDigest("code-414", "title-362")
        assertEquals(WeightedAvailabilityDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newWeightedAvailabilityDigest("code-414", "title-362")
        try {
            item.moveTo(WeightedAvailabilityDigestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAvailabilityDigestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedAvailabilityDigest("code-414", "title-362")
        item.status = WeightedAvailabilityDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedAvailabilityDigestStatus.REQUESTED) }
    }
}
