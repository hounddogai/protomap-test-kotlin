package com.acme.transport.fleetops.serviceplan.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalServicePlanBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalServicePlanBaseline("code-497", "label-319")
        assertEquals(RegionalServicePlanBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRegionalServicePlanBaseline("code-497", "label-319")
        try {
            item.moveTo(RegionalServicePlanBaselineStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalServicePlanBaselineStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalServicePlanBaseline("code-497", "label-319")
        item.status = RegionalServicePlanBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalServicePlanBaselineStatus.OPEN) }
    }
}
