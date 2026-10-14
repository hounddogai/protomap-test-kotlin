package com.acme.transport.network.zone.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneBundleTest {
    @Test
    fun `starts in new`() {
        val item = newZoneBundle("code-336", "title-99")
        assertEquals(ZoneBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newZoneBundle("code-336", "title-99")
        try {
            item.moveTo(ZoneBundleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneBundleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newZoneBundle("code-336", "title-99")
        item.status = ZoneBundleStatus.FAILED
        assertFails("move") { item.moveTo(ZoneBundleStatus.NEW) }
    }
}
