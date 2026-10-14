package com.acme.transport.network.transittime.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newTransitTimeTemplate("code-714", "name-993")
        assertEquals(TransitTimeTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newTransitTimeTemplate("code-714", "name-993")
        try {
            item.moveTo(TransitTimeTemplateStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeTemplateStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTransitTimeTemplate("code-714", "name-993")
        item.status = TransitTimeTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(TransitTimeTemplateStatus.PLANNED) }
    }
}
