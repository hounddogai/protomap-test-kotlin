package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newEvChargeRequest("code-579", "name-898")
        assertEquals(EvChargeRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newEvChargeRequest("code-579", "name-898")
        try {
            item.moveTo(EvChargeRequestStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeRequestStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newEvChargeRequest("code-579", "name-898")
        item.status = EvChargeRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(EvChargeRequestStatus.REQUESTED) }
    }
}
