package com.acme.transport.linehaul.laneplan.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newLanePlanException("code-444", "title-372")
        assertEquals(LanePlanExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLanePlanException("code-444", "title-372")
        try {
            item.moveTo(LanePlanExceptionStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanExceptionStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLanePlanException("code-444", "title-372")
        item.status = LanePlanExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(LanePlanExceptionStatus.IDLE) }
    }
}
