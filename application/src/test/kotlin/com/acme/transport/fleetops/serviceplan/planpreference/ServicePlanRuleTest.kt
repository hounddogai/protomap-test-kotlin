package com.acme.transport.fleetops.serviceplan.planpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanRuleTest {
    @Test
    fun `starts in open`() {
        val item = newServicePlanRule("code-41", "label-731")
        assertEquals(ServicePlanRuleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newServicePlanRule("code-41", "label-731")
        try {
            item.moveTo(ServicePlanRuleStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanRuleStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newServicePlanRule("code-41", "label-731")
        item.status = ServicePlanRuleStatus.ARCHIVED
        assertFails("move") { item.moveTo(ServicePlanRuleStatus.OPEN) }
    }
}
