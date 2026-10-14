package com.acme.transport.fleetops.utilization.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newUtilizationTarget("code-854", "title-250")
        assertEquals(UtilizationTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newUtilizationTarget("code-854", "title-250")
        try {
            item.moveTo(UtilizationTargetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationTargetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newUtilizationTarget("code-854", "title-250")
        item.status = UtilizationTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(UtilizationTargetStatus.PLANNED) }
    }
}
