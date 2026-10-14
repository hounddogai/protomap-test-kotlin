package com.acme.transport.terminal.dwell.dwelltemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellExceptionTest {
    @Test
    fun `starts in proposed`() {
        val item = newDwellException("code-571", "name-25")
        assertEquals(DwellExceptionStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDwellException("code-571", "name-25")
        try {
            item.moveTo(DwellExceptionStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellExceptionStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDwellException("code-571", "name-25")
        item.status = DwellExceptionStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DwellExceptionStatus.PROPOSED) }
    }
}
