package com.acme.transport.freightpay.accessorial.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialPlanTest {
    @Test
    fun `starts in open`() {
        val item = newAccessorialPlan("code-916", "label-597")
        assertEquals(AccessorialPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newAccessorialPlan("code-916", "label-597")
        try {
            item.moveTo(AccessorialPlanStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialPlanStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAccessorialPlan("code-916", "label-597")
        item.status = AccessorialPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(AccessorialPlanStatus.OPEN) }
    }
}
