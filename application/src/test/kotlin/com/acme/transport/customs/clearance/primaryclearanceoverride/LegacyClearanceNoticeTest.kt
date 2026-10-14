package com.acme.transport.customs.clearance.primaryclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyClearanceNoticeTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyClearanceNotice("code-998", "label-225")
        assertEquals(LegacyClearanceNoticeStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLegacyClearanceNotice("code-998", "label-225")
        try {
            item.moveTo(LegacyClearanceNoticeStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyClearanceNoticeStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyClearanceNotice("code-998", "label-225")
        item.status = LegacyClearanceNoticeStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyClearanceNoticeStatus.CREATED) }
    }
}
