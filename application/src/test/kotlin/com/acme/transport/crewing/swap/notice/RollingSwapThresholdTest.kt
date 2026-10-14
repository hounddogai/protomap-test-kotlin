package com.acme.transport.crewing.swap.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingSwapThresholdTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingSwapThreshold("code-954", "title-771")
        assertEquals(RollingSwapThresholdStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRollingSwapThreshold("code-954", "title-771")
        try {
            item.moveTo(RollingSwapThresholdStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingSwapThresholdStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingSwapThreshold("code-954", "title-771")
        item.status = RollingSwapThresholdStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingSwapThresholdStatus.PLANNED) }
    }
}
