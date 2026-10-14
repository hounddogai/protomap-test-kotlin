package com.acme.transport.network.transittime.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeTierTest {
    @Test
    fun `starts in created`() {
        val item = newTransitTimeTier("code-249", "title-611")
        assertEquals(TransitTimeTierStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTransitTimeTier("code-249", "title-611")
        try {
            item.moveTo(TransitTimeTierStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeTierStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTransitTimeTier("code-249", "title-611")
        item.status = TransitTimeTierStatus.RETIRED
        assertFails("move") { item.moveTo(TransitTimeTierStatus.CREATED) }
    }
}
