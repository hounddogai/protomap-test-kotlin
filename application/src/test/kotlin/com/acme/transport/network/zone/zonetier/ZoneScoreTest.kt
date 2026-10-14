package com.acme.transport.network.zone.zonetier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneScoreTest {
    @Test
    fun `starts in new`() {
        val item = newZoneScore("code-666", "title-724")
        assertEquals(ZoneScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newZoneScore("code-666", "title-724")
        try {
            item.moveTo(ZoneScoreStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneScoreStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newZoneScore("code-666", "title-724")
        item.status = ZoneScoreStatus.FAILED
        assertFails("move") { item.moveTo(ZoneScoreStatus.NEW) }
    }
}
