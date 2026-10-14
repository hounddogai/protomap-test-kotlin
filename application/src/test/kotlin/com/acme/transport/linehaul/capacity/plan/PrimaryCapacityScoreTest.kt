package com.acme.transport.linehaul.capacity.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCapacityScoreTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryCapacityScore("code-940", "name-529")
        assertEquals(PrimaryCapacityScoreStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryCapacityScore("code-940", "name-529")
        try {
            item.moveTo(PrimaryCapacityScoreStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCapacityScoreStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryCapacityScore("code-940", "name-529")
        item.status = PrimaryCapacityScoreStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryCapacityScoreStatus.REQUESTED) }
    }
}
