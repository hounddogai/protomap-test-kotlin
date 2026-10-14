package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryClearanceGroupTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryClearanceGroup("code-422", "name-751")
        assertEquals(PrimaryClearanceGroupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPrimaryClearanceGroup("code-422", "name-751")
        try {
            item.moveTo(PrimaryClearanceGroupStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryClearanceGroupStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryClearanceGroup("code-422", "name-751")
        item.status = PrimaryClearanceGroupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryClearanceGroupStatus.PROPOSED) }
    }
}
