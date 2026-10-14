package com.acme.transport.emissions.evcharge.chargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newEvChargeCycle("code-777", "label-981")
        assertEquals(EvChargeCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newEvChargeCycle("code-777", "label-981")
        try {
            item.moveTo(EvChargeCycleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeCycleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newEvChargeCycle("code-777", "label-981")
        item.status = EvChargeCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(EvChargeCycleStatus.PLANNED) }
    }
}
