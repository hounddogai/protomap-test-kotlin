package com.acme.transport.linehaul.laneplan.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLanePlanQuotaTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledLanePlanQuota("code-305", "label-264")
        assertEquals(ScheduledLanePlanQuotaStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScheduledLanePlanQuota("code-305", "label-264")
        try {
            item.moveTo(ScheduledLanePlanQuotaStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLanePlanQuotaStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledLanePlanQuota("code-305", "label-264")
        item.status = ScheduledLanePlanQuotaStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledLanePlanQuotaStatus.NEW) }
    }
}
