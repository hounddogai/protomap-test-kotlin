package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryServicePlanProfileTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryServicePlanProfile("code-874", "label-361")
        assertEquals(PrimaryServicePlanProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPrimaryServicePlanProfile("code-874", "label-361")
        try {
            item.moveTo(PrimaryServicePlanProfileStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryServicePlanProfileStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryServicePlanProfile("code-874", "label-361")
        item.status = PrimaryServicePlanProfileStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryServicePlanProfileStatus.CREATED) }
    }
}
