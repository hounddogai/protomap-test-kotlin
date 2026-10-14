package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackInspectionNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackInspectionNotice("code-314", "name-877")
        assertEquals(FallbackInspectionNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newFallbackInspectionNotice("code-314", "name-877")
        try {
            item.moveTo(FallbackInspectionNoticeStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackInspectionNoticeStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackInspectionNotice("code-314", "name-877")
        item.status = FallbackInspectionNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackInspectionNoticeStatus.PROPOSED) }
    }
}
