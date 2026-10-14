package com.acme.transport.network.node.nodegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingNodeTierTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingNodeTier("code-886", "name-278")
        assertEquals(RollingNodeTierStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRollingNodeTier("code-886", "name-278")
        try {
            item.moveTo(RollingNodeTierStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingNodeTierStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingNodeTier("code-886", "name-278")
        item.status = RollingNodeTierStatus.CLOSED
        assertFails("move") { item.moveTo(RollingNodeTierStatus.DRAFT) }
    }
}
