package com.acme.transport.customs.hold.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newHoldEstimate("code-547", "name-507")
        assertEquals(HoldEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newHoldEstimate("code-547", "name-507")
        try {
            item.moveTo(HoldEstimateStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldEstimateStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newHoldEstimate("code-547", "name-507")
        item.status = HoldEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(HoldEstimateStatus.REQUESTED) }
    }
}
