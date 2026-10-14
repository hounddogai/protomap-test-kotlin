package com.acme.transport.lastmile.redelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRedeliveryEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientRedeliveryEstimate("code-115", "title-954")
        assertEquals(LenientRedeliveryEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLenientRedeliveryEstimate("code-115", "title-954")
        try {
            item.moveTo(LenientRedeliveryEstimateStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRedeliveryEstimateStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientRedeliveryEstimate("code-115", "title-954")
        item.status = LenientRedeliveryEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientRedeliveryEstimateStatus.REQUESTED) }
    }
}
