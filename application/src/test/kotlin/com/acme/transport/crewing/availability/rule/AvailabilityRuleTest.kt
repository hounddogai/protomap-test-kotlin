package com.acme.transport.crewing.availability.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newAvailabilityRule("code-703", "title-857")
        assertEquals(AvailabilityRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAvailabilityRule("code-703", "title-857")
        try {
            item.moveTo(AvailabilityRuleStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityRuleStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAvailabilityRule("code-703", "title-857")
        item.status = AvailabilityRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(AvailabilityRuleStatus.REQUESTED) }
    }
}
