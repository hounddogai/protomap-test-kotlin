package com.acme.transport.network.transittime.timeoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTransitTimeOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryTransitTimeOverride("code-99", "title-59")
        assertEquals(PrimaryTransitTimeOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newPrimaryTransitTimeOverride("code-99", "title-59")
        try {
            item.moveTo(PrimaryTransitTimeOverrideStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTransitTimeOverrideStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryTransitTimeOverride("code-99", "title-59")
        item.status = PrimaryTransitTimeOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryTransitTimeOverrideStatus.OPEN) }
    }
}
