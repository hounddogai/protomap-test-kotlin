package com.acme.transport.fleetops.utilization.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newUtilizationPolicy("code-978", "title-719")
        assertEquals(UtilizationPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newUtilizationPolicy("code-978", "title-719")
        try {
            item.moveTo(UtilizationPolicyStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationPolicyStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newUtilizationPolicy("code-978", "title-719")
        item.status = UtilizationPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(UtilizationPolicyStatus.CREATED) }
    }
}
