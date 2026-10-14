package com.acme.transport.lastmile.drop.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDropThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredDropThreshold("code-213", "name-849")
        assertEquals(TieredDropThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTieredDropThreshold("code-213", "name-849")
        try {
            item.moveTo(TieredDropThresholdStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDropThresholdStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredDropThreshold("code-213", "name-849")
        item.status = TieredDropThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(TieredDropThresholdStatus.IDLE) }
    }
}
