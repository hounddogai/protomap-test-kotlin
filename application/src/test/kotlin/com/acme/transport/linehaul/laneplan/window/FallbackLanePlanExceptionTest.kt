package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackLanePlanExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackLanePlanException("code-957", "label-606")
        assertEquals(FallbackLanePlanExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFallbackLanePlanException("code-957", "label-606")
        try {
            item.moveTo(FallbackLanePlanExceptionStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackLanePlanExceptionStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackLanePlanException("code-957", "label-606")
        item.status = FallbackLanePlanExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackLanePlanExceptionStatus.IDLE) }
    }
}
