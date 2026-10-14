package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryServicePlanNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryServicePlanNotice("code-72", "label-196")
        assertEquals(PrimaryServicePlanNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPrimaryServicePlanNotice("code-72", "label-196")
        try {
            item.moveTo(PrimaryServicePlanNoticeStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryServicePlanNoticeStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryServicePlanNotice("code-72", "label-196")
        item.status = PrimaryServicePlanNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryServicePlanNoticeStatus.PROPOSED) }
    }
}
