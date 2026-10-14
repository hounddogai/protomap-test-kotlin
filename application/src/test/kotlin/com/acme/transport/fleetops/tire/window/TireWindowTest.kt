package com.acme.transport.fleetops.tire.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireWindowTest {
    @Test
    fun `starts in created`() {
        val item = newTireWindow("code-629", "title-243")
        assertEquals(TireWindowStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTireWindow("code-629", "title-243")
        try {
            item.moveTo(TireWindowStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireWindowStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTireWindow("code-629", "title-243")
        item.status = TireWindowStatus.RETIRED
        assertFails("move") { item.moveTo(TireWindowStatus.CREATED) }
    }
}
