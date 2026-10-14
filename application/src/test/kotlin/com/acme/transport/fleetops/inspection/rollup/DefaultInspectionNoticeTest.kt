package com.acme.transport.fleetops.inspection.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultInspectionNoticeTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultInspectionNotice("code-288", "label-898")
        assertEquals(DefaultInspectionNoticeStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultInspectionNotice("code-288", "label-898")
        try {
            item.moveTo(DefaultInspectionNoticeStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultInspectionNoticeStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultInspectionNotice("code-288", "label-898")
        item.status = DefaultInspectionNoticeStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultInspectionNoticeStatus.CREATED) }
    }
}
