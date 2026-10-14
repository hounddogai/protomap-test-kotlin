package com.acme.transport.linehaul.laneplan.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyLanePlanQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyLanePlanQuota("code-329", "title-877")
        assertEquals(LegacyLanePlanQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyLanePlanQuota("code-329", "title-877")
        try {
            item.moveTo(LegacyLanePlanQuotaStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyLanePlanQuotaStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyLanePlanQuota("code-329", "title-877")
        item.status = LegacyLanePlanQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyLanePlanQuotaStatus.REQUESTED) }
    }
}
