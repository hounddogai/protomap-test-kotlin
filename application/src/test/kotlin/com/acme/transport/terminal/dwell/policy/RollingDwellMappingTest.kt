package com.acme.transport.terminal.dwell.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDwellMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingDwellMapping("code-578", "label-151")
        assertEquals(RollingDwellMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRollingDwellMapping("code-578", "label-151")
        try {
            item.moveTo(RollingDwellMappingStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDwellMappingStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingDwellMapping("code-578", "label-151")
        item.status = RollingDwellMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingDwellMappingStatus.PLANNED) }
    }
}
