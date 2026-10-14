package com.acme.transport.fleetops.serviceplan.legacyplanquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalServicePlanTargetTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalServicePlanTarget("code-326", "label-669")
        assertEquals(GlobalServicePlanTargetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newGlobalServicePlanTarget("code-326", "label-669")
        try {
            item.moveTo(GlobalServicePlanTargetStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalServicePlanTargetStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalServicePlanTarget("code-326", "label-669")
        item.status = GlobalServicePlanTargetStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalServicePlanTargetStatus.OPEN) }
    }
}
