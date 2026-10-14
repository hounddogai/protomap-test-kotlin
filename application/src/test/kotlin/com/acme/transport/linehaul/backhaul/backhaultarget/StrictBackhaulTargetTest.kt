package com.acme.transport.linehaul.backhaul.backhaultarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictBackhaulTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictBackhaulTarget("code-462", "title-647")
        assertEquals(StrictBackhaulTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newStrictBackhaulTarget("code-462", "title-647")
        try {
            item.moveTo(StrictBackhaulTargetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictBackhaulTargetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictBackhaulTarget("code-462", "title-647")
        item.status = StrictBackhaulTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictBackhaulTargetStatus.PLANNED) }
    }
}
