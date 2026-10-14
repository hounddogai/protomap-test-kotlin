package com.acme.transport.linehaul.laneplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedLanePlanExceptionTest {
    @Test
    fun `starts in open`() {
        val item = newCappedLanePlanException("code-209", "name-256")
        assertEquals(CappedLanePlanExceptionStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newCappedLanePlanException("code-209", "name-256")
        try {
            item.moveTo(CappedLanePlanExceptionStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedLanePlanExceptionStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedLanePlanException("code-209", "name-256")
        item.status = CappedLanePlanExceptionStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedLanePlanExceptionStatus.OPEN) }
    }
}
