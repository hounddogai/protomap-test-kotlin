package com.acme.transport.lastmile.drop.dropnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDropPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictDropPolicy("code-368", "name-895")
        assertEquals(StrictDropPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newStrictDropPolicy("code-368", "name-895")
        try {
            item.moveTo(StrictDropPolicyStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDropPolicyStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictDropPolicy("code-368", "name-895")
        item.status = StrictDropPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(StrictDropPolicyStatus.IDLE) }
    }
}
