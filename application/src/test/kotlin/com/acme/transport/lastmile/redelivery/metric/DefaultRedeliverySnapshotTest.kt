package com.acme.transport.lastmile.redelivery.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRedeliverySnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultRedeliverySnapshot("code-569", "title-307")
        assertEquals(DefaultRedeliverySnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDefaultRedeliverySnapshot("code-569", "title-307")
        try {
            item.moveTo(DefaultRedeliverySnapshotStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRedeliverySnapshotStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultRedeliverySnapshot("code-569", "title-307")
        item.status = DefaultRedeliverySnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultRedeliverySnapshotStatus.IDLE) }
    }
}
