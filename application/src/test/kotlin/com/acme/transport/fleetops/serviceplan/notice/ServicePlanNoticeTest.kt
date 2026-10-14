package com.acme.transport.fleetops.serviceplan.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newServicePlanNotice("code-825", "name-501")
        assertEquals(ServicePlanNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newServicePlanNotice("code-825", "name-501")
        try {
            item.moveTo(ServicePlanNoticeStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanNoticeStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newServicePlanNotice("code-825", "name-501")
        item.status = ServicePlanNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(ServicePlanNoticeStatus.PLANNED) }
    }
}
