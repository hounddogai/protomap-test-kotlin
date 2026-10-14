package com.acme.transport.fleetops.serviceplan.planrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackServicePlanNoticeTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackServicePlanNotice("code-669", "title-771")
        assertEquals(FallbackServicePlanNoticeStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFallbackServicePlanNotice("code-669", "title-771")
        try {
            item.moveTo(FallbackServicePlanNoticeStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackServicePlanNoticeStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackServicePlanNotice("code-669", "title-771")
        item.status = FallbackServicePlanNoticeStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackServicePlanNoticeStatus.REQUESTED) }
    }
}
