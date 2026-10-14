package com.acme.transport.network.node.nodesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalNodeSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalNodeSnapshot("code-900", "name-302")
        assertEquals(RegionalNodeSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newRegionalNodeSnapshot("code-900", "name-302")
        try {
            item.moveTo(RegionalNodeSnapshotStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalNodeSnapshotStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalNodeSnapshot("code-900", "name-302")
        item.status = RegionalNodeSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalNodeSnapshotStatus.CREATED) }
    }
}
