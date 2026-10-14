package com.acme.transport.network.transittime.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeDraftTest {
    @Test
    fun `starts in created`() {
        val item = newTransitTimeDraft("code-865", "name-507")
        assertEquals(TransitTimeDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newTransitTimeDraft("code-865", "name-507")
        try {
            item.moveTo(TransitTimeDraftStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeDraftStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTransitTimeDraft("code-865", "name-507")
        item.status = TransitTimeDraftStatus.RETIRED
        assertFails("move") { item.moveTo(TransitTimeDraftStatus.CREATED) }
    }
}
