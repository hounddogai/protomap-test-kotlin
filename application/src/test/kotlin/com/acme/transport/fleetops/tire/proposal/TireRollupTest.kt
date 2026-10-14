package com.acme.transport.fleetops.tire.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireRollupTest {
    @Test
    fun `starts in proposed`() {
        val item = newTireRollup("code-966", "label-745")
        assertEquals(TireRollupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTireRollup("code-966", "label-745")
        try {
            item.moveTo(TireRollupStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireRollupStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTireRollup("code-966", "label-745")
        item.status = TireRollupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TireRollupStatus.PROPOSED) }
    }
}
