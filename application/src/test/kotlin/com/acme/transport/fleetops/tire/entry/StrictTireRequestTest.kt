package com.acme.transport.fleetops.tire.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTireRequestTest {
    @Test
    fun `starts in new`() {
        val item = newStrictTireRequest("code-710", "title-597")
        assertEquals(StrictTireRequestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newStrictTireRequest("code-710", "title-597")
        try {
            item.moveTo(StrictTireRequestStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTireRequestStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictTireRequest("code-710", "title-597")
        item.status = StrictTireRequestStatus.FAILED
        assertFails("move") { item.moveTo(StrictTireRequestStatus.NEW) }
    }
}
