package com.acme.transport.customs.hold.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldRollupTest {
    @Test
    fun `starts in requested`() {
        val item = newHoldRollup("code-15", "label-830")
        assertEquals(HoldRollupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newHoldRollup("code-15", "label-830")
        try {
            item.moveTo(HoldRollupStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldRollupStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newHoldRollup("code-15", "label-830")
        item.status = HoldRollupStatus.EXPIRED
        assertFails("move") { item.moveTo(HoldRollupStatus.REQUESTED) }
    }
}
