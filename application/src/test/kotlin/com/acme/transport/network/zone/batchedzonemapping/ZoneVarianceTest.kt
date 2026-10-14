package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newZoneVariance("code-359", "label-497")
        assertEquals(ZoneVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newZoneVariance("code-359", "label-497")
        try {
            item.moveTo(ZoneVarianceStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneVarianceStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newZoneVariance("code-359", "label-497")
        item.status = ZoneVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(ZoneVarianceStatus.OPEN) }
    }
}
