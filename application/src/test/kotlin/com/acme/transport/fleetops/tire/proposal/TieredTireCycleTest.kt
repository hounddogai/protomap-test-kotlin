package com.acme.transport.fleetops.tire.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTireCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredTireCycle("code-665", "title-605")
        assertEquals(TieredTireCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredTireCycle("code-665", "title-605")
        try {
            item.moveTo(TieredTireCycleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTireCycleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredTireCycle("code-665", "title-605")
        item.status = TieredTireCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredTireCycleStatus.PROPOSED) }
    }
}
