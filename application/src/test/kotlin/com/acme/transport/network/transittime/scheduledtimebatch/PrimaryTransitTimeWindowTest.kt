package com.acme.transport.network.transittime.scheduledtimebatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTransitTimeWindowTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryTransitTimeWindow("code-790", "title-858")
        assertEquals(PrimaryTransitTimeWindowStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPrimaryTransitTimeWindow("code-790", "title-858")
        try {
            item.moveTo(PrimaryTransitTimeWindowStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTransitTimeWindowStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryTransitTimeWindow("code-790", "title-858")
        item.status = PrimaryTransitTimeWindowStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryTransitTimeWindowStatus.CREATED) }
    }
}
