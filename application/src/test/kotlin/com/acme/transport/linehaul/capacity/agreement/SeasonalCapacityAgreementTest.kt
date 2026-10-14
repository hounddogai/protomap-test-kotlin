package com.acme.transport.linehaul.capacity.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCapacityAgreementTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalCapacityAgreement("code-385", "name-926")
        assertEquals(SeasonalCapacityAgreementStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalCapacityAgreement("code-385", "name-926")
        try {
            item.moveTo(SeasonalCapacityAgreementStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCapacityAgreementStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalCapacityAgreement("code-385", "name-926")
        item.status = SeasonalCapacityAgreementStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalCapacityAgreementStatus.OPEN) }
    }
}
