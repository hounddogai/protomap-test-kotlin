package com.acme.transport.lastmile.parcel.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelPlanTest {
    @Test
    fun `starts in created`() {
        val item = newParcelPlan("code-58", "name-108")
        assertEquals(ParcelPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newParcelPlan("code-58", "name-108")
        try {
            item.moveTo(ParcelPlanStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelPlanStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newParcelPlan("code-58", "name-108")
        item.status = ParcelPlanStatus.RETIRED
        assertFails("move") { item.moveTo(ParcelPlanStatus.CREATED) }
    }
}
