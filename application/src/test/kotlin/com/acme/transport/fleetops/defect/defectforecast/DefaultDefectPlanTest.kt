package com.acme.transport.fleetops.defect.defectforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDefectPlanTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultDefectPlan("code-571", "title-602")
        assertEquals(DefaultDefectPlanStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefaultDefectPlan("code-571", "title-602")
        try {
            item.moveTo(DefaultDefectPlanStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDefectPlanStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultDefectPlan("code-571", "title-602")
        item.status = DefaultDefectPlanStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultDefectPlanStatus.REQUESTED) }
    }
}
