package com.acme.transport.network.servicearea.areacycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledServiceAreaRuleTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledServiceAreaRule("code-414", "name-505")
        assertEquals(ScheduledServiceAreaRuleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newScheduledServiceAreaRule("code-414", "name-505")
        try {
            item.moveTo(ScheduledServiceAreaRuleStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledServiceAreaRuleStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledServiceAreaRule("code-414", "name-505")
        item.status = ScheduledServiceAreaRuleStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledServiceAreaRuleStatus.PLANNED) }
    }
}
