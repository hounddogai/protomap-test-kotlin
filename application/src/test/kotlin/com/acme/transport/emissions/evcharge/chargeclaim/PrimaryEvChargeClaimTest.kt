package com.acme.transport.emissions.evcharge.chargeclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryEvChargeClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryEvChargeClaim("code-909", "label-644")
        assertEquals(PrimaryEvChargeClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPrimaryEvChargeClaim("code-909", "label-644")
        try {
            item.moveTo(PrimaryEvChargeClaimStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryEvChargeClaimStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryEvChargeClaim("code-909", "label-644")
        item.status = PrimaryEvChargeClaimStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryEvChargeClaimStatus.IDLE) }
    }
}
