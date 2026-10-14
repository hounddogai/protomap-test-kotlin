package com.acme.transport.network.servicearea.manualareathreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictServiceAreaBundleTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictServiceAreaBundle("code-555", "name-647")
        assertEquals(StrictServiceAreaBundleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newStrictServiceAreaBundle("code-555", "name-647")
        try {
            item.moveTo(StrictServiceAreaBundleStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictServiceAreaBundleStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictServiceAreaBundle("code-555", "name-647")
        item.status = StrictServiceAreaBundleStatus.CLOSED
        assertFails("move") { item.moveTo(StrictServiceAreaBundleStatus.DRAFT) }
    }
}
