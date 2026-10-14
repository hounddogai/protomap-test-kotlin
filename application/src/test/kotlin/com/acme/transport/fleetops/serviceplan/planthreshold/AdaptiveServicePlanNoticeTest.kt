package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveServicePlanNoticeTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveServicePlanNotice("code-389", "label-747")
        assertEquals(AdaptiveServicePlanNoticeStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newAdaptiveServicePlanNotice("code-389", "label-747")
        try {
            item.moveTo(AdaptiveServicePlanNoticeStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveServicePlanNoticeStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveServicePlanNotice("code-389", "label-747")
        item.status = AdaptiveServicePlanNoticeStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveServicePlanNoticeStatus.DRAFT) }
    }
}
