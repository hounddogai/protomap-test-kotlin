package com.acme.transport.crewing.swap.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingSwapTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingSwapTarget("code-460", "name-659")
        assertEquals(RollingSwapTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingSwapTarget("code-460", "name-659")
        try {
            item.moveTo(RollingSwapTargetStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingSwapTargetStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingSwapTarget("code-460", "name-659")
        item.status = RollingSwapTargetStatus.STOPPED
        assertFails("move") { item.moveTo(RollingSwapTargetStatus.IDLE) }
    }
}
