package com.acme.transport.linehaul.laneplan.planreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackLanePlanNoticeTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackLanePlanNotice("code-34", "title-767")
        assertEquals(FallbackLanePlanNoticeStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newFallbackLanePlanNotice("code-34", "title-767")
        try {
            item.moveTo(FallbackLanePlanNoticeStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackLanePlanNoticeStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackLanePlanNotice("code-34", "title-767")
        item.status = FallbackLanePlanNoticeStatus.FAILED
        assertFails("move") { item.moveTo(FallbackLanePlanNoticeStatus.NEW) }
    }
}
