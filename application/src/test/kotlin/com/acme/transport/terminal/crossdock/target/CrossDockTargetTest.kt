package com.acme.transport.terminal.crossdock.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockTargetTest {
    @Test
    fun `starts in created`() {
        val item = newCrossDockTarget("code-614", "name-882")
        assertEquals(CrossDockTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCrossDockTarget("code-614", "name-882")
        try {
            item.moveTo(CrossDockTargetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockTargetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCrossDockTarget("code-614", "name-882")
        item.status = CrossDockTargetStatus.RETIRED
        assertFails("move") { item.moveTo(CrossDockTargetStatus.CREATED) }
    }
}
