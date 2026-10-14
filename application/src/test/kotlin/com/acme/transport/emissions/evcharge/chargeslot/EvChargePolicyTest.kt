package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargePolicyTest {
    @Test
    fun `starts in draft`() {
        val item = newEvChargePolicy("code-632", "name-537")
        assertEquals(EvChargePolicyStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newEvChargePolicy("code-632", "name-537")
        try {
            item.moveTo(EvChargePolicyStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargePolicyStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newEvChargePolicy("code-632", "name-537")
        item.status = EvChargePolicyStatus.CLOSED
        assertFails("move") { item.moveTo(EvChargePolicyStatus.DRAFT) }
    }
}
