package com.acme.transport.fleetops.tire.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newTireOverride("code-906", "label-457")
        assertEquals(TireOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTireOverride("code-906", "label-457")
        try {
            item.moveTo(TireOverrideStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireOverrideStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTireOverride("code-906", "label-457")
        item.status = TireOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(TireOverrideStatus.CREATED) }
    }
}
