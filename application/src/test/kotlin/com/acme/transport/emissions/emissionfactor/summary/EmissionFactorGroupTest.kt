package com.acme.transport.emissions.emissionfactor.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newEmissionFactorGroup("code-590", "label-810")
        assertEquals(EmissionFactorGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newEmissionFactorGroup("code-590", "label-810")
        try {
            item.moveTo(EmissionFactorGroupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorGroupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newEmissionFactorGroup("code-590", "label-810")
        item.status = EmissionFactorGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(EmissionFactorGroupStatus.REQUESTED) }
    }
}
