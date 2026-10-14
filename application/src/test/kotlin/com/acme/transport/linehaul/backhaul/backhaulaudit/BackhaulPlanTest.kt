package com.acme.transport.linehaul.backhaul.backhaulaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulPlanTest {
    @Test
    fun `starts in planned`() {
        val item = newBackhaulPlan("code-281", "title-571")
        assertEquals(BackhaulPlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBackhaulPlan("code-281", "title-571")
        try {
            item.moveTo(BackhaulPlanStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulPlanStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBackhaulPlan("code-281", "title-571")
        item.status = BackhaulPlanStatus.CANCELLED
        assertFails("move") { item.moveTo(BackhaulPlanStatus.PLANNED) }
    }
}
