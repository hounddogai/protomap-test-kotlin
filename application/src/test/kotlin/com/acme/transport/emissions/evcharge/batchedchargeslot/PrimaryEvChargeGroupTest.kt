package com.acme.transport.emissions.evcharge.batchedchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryEvChargeGroupTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryEvChargeGroup("code-238", "name-766")
        assertEquals(PrimaryEvChargeGroupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newPrimaryEvChargeGroup("code-238", "name-766")
        try {
            item.moveTo(PrimaryEvChargeGroupStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryEvChargeGroupStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryEvChargeGroup("code-238", "name-766")
        item.status = PrimaryEvChargeGroupStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryEvChargeGroupStatus.OPEN) }
    }
}
