package com.acme.transport.linehaul.capacity.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityBaselineTest {
    @Test
    fun `starts in created`() {
        val item = newCapacityBaseline("code-986", "title-343")
        assertEquals(CapacityBaselineStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newCapacityBaseline("code-986", "title-343")
        try {
            item.moveTo(CapacityBaselineStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityBaselineStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCapacityBaseline("code-986", "title-343")
        item.status = CapacityBaselineStatus.RETIRED
        assertFails("move") { item.moveTo(CapacityBaselineStatus.CREATED) }
    }
}
