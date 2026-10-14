package com.acme.transport.fleetops.utilization.utilizationledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryUtilizationTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryUtilizationTarget("code-280", "label-483")
        assertEquals(PrimaryUtilizationTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryUtilizationTarget("code-280", "label-483")
        try {
            item.moveTo(PrimaryUtilizationTargetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryUtilizationTargetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryUtilizationTarget("code-280", "label-483")
        item.status = PrimaryUtilizationTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryUtilizationTargetStatus.REQUESTED) }
    }
}
