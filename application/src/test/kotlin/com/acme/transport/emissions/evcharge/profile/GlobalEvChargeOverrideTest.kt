package com.acme.transport.emissions.evcharge.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalEvChargeOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalEvChargeOverride("code-677", "name-98")
        assertEquals(GlobalEvChargeOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalEvChargeOverride("code-677", "name-98")
        try {
            item.moveTo(GlobalEvChargeOverrideStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalEvChargeOverrideStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalEvChargeOverride("code-677", "name-98")
        item.status = GlobalEvChargeOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalEvChargeOverrideStatus.REQUESTED) }
    }
}
