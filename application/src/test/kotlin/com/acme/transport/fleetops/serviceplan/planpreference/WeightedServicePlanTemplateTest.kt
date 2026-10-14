package com.acme.transport.fleetops.serviceplan.planpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedServicePlanTemplateTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedServicePlanTemplate("code-969", "title-7")
        assertEquals(WeightedServicePlanTemplateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newWeightedServicePlanTemplate("code-969", "title-7")
        try {
            item.moveTo(WeightedServicePlanTemplateStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedServicePlanTemplateStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedServicePlanTemplate("code-969", "title-7")
        item.status = WeightedServicePlanTemplateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedServicePlanTemplateStatus.PROPOSED) }
    }
}
