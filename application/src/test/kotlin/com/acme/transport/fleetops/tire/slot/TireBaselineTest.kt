package com.acme.transport.fleetops.tire.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireBaselineTest {
    @Test
    fun `starts in new`() {
        val item = newTireBaseline("code-293", "title-499")
        assertEquals(TireBaselineStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newTireBaseline("code-293", "title-499")
        try {
            item.moveTo(TireBaselineStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireBaselineStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTireBaseline("code-293", "title-499")
        item.status = TireBaselineStatus.FAILED
        assertFails("move") { item.moveTo(TireBaselineStatus.NEW) }
    }
}
