package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryServicePlanMappingTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryServicePlanMapping("code-174", "title-422")
        assertEquals(PrimaryServicePlanMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPrimaryServicePlanMapping("code-174", "title-422")
        try {
            item.moveTo(PrimaryServicePlanMappingStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryServicePlanMappingStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryServicePlanMapping("code-174", "title-422")
        item.status = PrimaryServicePlanMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryServicePlanMappingStatus.OPEN) }
    }
}
