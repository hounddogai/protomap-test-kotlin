package com.acme.transport.network.hub.primaryhubsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalHubSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalHubSnapshot("code-878", "name-586")
        assertEquals(RegionalHubSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRegionalHubSnapshot("code-878", "name-586")
        try {
            item.moveTo(RegionalHubSnapshotStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalHubSnapshotStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalHubSnapshot("code-878", "name-586")
        item.status = RegionalHubSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(RegionalHubSnapshotStatus.NEW) }
    }
}
