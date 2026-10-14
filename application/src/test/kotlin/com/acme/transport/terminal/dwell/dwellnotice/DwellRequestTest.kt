package com.acme.transport.terminal.dwell.dwellnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellRequestTest {
    @Test
    fun `starts in new`() {
        val item = newDwellRequest("code-639", "title-921")
        assertEquals(DwellRequestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDwellRequest("code-639", "title-921")
        try {
            item.moveTo(DwellRequestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellRequestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDwellRequest("code-639", "title-921")
        item.status = DwellRequestStatus.FAILED
        assertFails("move") { item.moveTo(DwellRequestStatus.NEW) }
    }
}
