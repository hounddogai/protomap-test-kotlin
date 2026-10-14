package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedUtilizationPreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedUtilizationPreference("code-257", "name-911")
        assertEquals(CappedUtilizationPreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCappedUtilizationPreference("code-257", "name-911")
        try {
            item.moveTo(CappedUtilizationPreferenceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedUtilizationPreferenceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedUtilizationPreference("code-257", "name-911")
        item.status = CappedUtilizationPreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedUtilizationPreferenceStatus.PROPOSED) }
    }
}
