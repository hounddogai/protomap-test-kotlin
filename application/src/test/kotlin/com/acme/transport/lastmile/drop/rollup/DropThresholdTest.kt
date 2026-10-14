package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newDropThreshold("code-653", "title-233")
        assertEquals(DropThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDropThreshold("code-653", "title-233")
        try {
            item.moveTo(DropThresholdStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropThresholdStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDropThreshold("code-653", "title-233")
        item.status = DropThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(DropThresholdStatus.IDLE) }
    }
}
