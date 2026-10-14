package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRestBreakWindowTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalRestBreakWindow("code-755", "title-474")
        assertEquals(GlobalRestBreakWindowStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newGlobalRestBreakWindow("code-755", "title-474")
        try {
            item.moveTo(GlobalRestBreakWindowStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRestBreakWindowStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalRestBreakWindow("code-755", "title-474")
        item.status = GlobalRestBreakWindowStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalRestBreakWindowStatus.IDLE) }
    }
}
