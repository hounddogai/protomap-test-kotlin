package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCapacityRollupTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingCapacityRollup("code-839", "title-436")
        assertEquals(RollingCapacityRollupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRollingCapacityRollup("code-839", "title-436")
        try {
            item.moveTo(RollingCapacityRollupStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCapacityRollupStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingCapacityRollup("code-839", "title-436")
        item.status = RollingCapacityRollupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingCapacityRollupStatus.PROPOSED) }
    }
}
