package com.acme.transport.terminal.traileryard.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardQuotaTest {
    @Test
    fun `starts in created`() {
        val item = newTrailerYardQuota("code-567", "label-364")
        assertEquals(TrailerYardQuotaStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newTrailerYardQuota("code-567", "label-364")
        try {
            item.moveTo(TrailerYardQuotaStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardQuotaStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTrailerYardQuota("code-567", "label-364")
        item.status = TrailerYardQuotaStatus.RETIRED
        assertFails("move") { item.moveTo(TrailerYardQuotaStatus.CREATED) }
    }
}
