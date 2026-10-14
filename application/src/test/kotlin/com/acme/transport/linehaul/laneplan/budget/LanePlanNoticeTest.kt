package com.acme.transport.linehaul.laneplan.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanNoticeTest {
    @Test
    fun `starts in created`() {
        val item = newLanePlanNotice("code-505", "name-556")
        assertEquals(LanePlanNoticeStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLanePlanNotice("code-505", "name-556")
        try {
            item.moveTo(LanePlanNoticeStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanNoticeStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLanePlanNotice("code-505", "name-556")
        item.status = LanePlanNoticeStatus.RETIRED
        assertFails("move") { item.moveTo(LanePlanNoticeStatus.CREATED) }
    }
}
