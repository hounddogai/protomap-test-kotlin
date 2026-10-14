package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockQuotaTest {
    @Test
    fun `starts in created`() {
        val item = newCrossDockQuota("code-313", "name-878")
        assertEquals(CrossDockQuotaStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newCrossDockQuota("code-313", "name-878")
        try {
            item.moveTo(CrossDockQuotaStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockQuotaStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCrossDockQuota("code-313", "name-878")
        item.status = CrossDockQuotaStatus.RETIRED
        assertFails("move") { item.moveTo(CrossDockQuotaStatus.CREATED) }
    }
}
