package com.acme.transport.emissions.fuelburn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientFuelBurnWindowTest {
    @Test
    fun `starts in new`() {
        val item = newLenientFuelBurnWindow("code-620", "title-111")
        assertEquals(LenientFuelBurnWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newLenientFuelBurnWindow("code-620", "title-111")
        try {
            item.moveTo(LenientFuelBurnWindowStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientFuelBurnWindowStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientFuelBurnWindow("code-620", "title-111")
        item.status = LenientFuelBurnWindowStatus.FAILED
        assertFails("move") { item.moveTo(LenientFuelBurnWindowStatus.NEW) }
    }
}
