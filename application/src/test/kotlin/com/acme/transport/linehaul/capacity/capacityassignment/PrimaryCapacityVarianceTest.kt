package com.acme.transport.linehaul.capacity.capacityassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCapacityVarianceTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryCapacityVariance("code-485", "title-80")
        assertEquals(PrimaryCapacityVarianceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryCapacityVariance("code-485", "title-80")
        try {
            item.moveTo(PrimaryCapacityVarianceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCapacityVarianceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryCapacityVariance("code-485", "title-80")
        item.status = PrimaryCapacityVarianceStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryCapacityVarianceStatus.REQUESTED) }
    }
}
