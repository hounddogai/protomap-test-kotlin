package com.acme.transport.lastmile.drop.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropTierTest {
    @Test
    fun `starts in idle`() {
        val item = newDropTier("code-845", "label-598")
        assertEquals(DropTierStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDropTier("code-845", "label-598")
        try {
            item.moveTo(DropTierStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropTierStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDropTier("code-845", "label-598")
        item.status = DropTierStatus.STOPPED
        assertFails("move") { item.moveTo(DropTierStatus.IDLE) }
    }
}
