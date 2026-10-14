package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceNoticeTest {
    @Test
    fun `starts in created`() {
        val item = newClearanceNotice("code-719", "label-849")
        assertEquals(ClearanceNoticeStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newClearanceNotice("code-719", "label-849")
        try {
            item.moveTo(ClearanceNoticeStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceNoticeStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newClearanceNotice("code-719", "label-849")
        item.status = ClearanceNoticeStatus.RETIRED
        assertFails("move") { item.moveTo(ClearanceNoticeStatus.CREATED) }
    }
}
