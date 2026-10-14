package com.acme.transport.linehaul.laneplan.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanPolicyTest {
    @Test
    fun `starts in planned`() {
        val item = newLanePlanPolicy("code-566", "name-921")
        assertEquals(LanePlanPolicyStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLanePlanPolicy("code-566", "name-921")
        try {
            item.moveTo(LanePlanPolicyStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanPolicyStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLanePlanPolicy("code-566", "name-921")
        item.status = LanePlanPolicyStatus.CANCELLED
        assertFails("move") { item.moveTo(LanePlanPolicyStatus.PLANNED) }
    }
}
