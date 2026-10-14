package com.acme.transport.fleetops.serviceplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingServicePlanPreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingServicePlanPreference("code-735", "title-873")
        assertEquals(PendingServicePlanPreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newPendingServicePlanPreference("code-735", "title-873")
        try {
            item.moveTo(PendingServicePlanPreferenceStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingServicePlanPreferenceStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingServicePlanPreference("code-735", "title-873")
        item.status = PendingServicePlanPreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingServicePlanPreferenceStatus.PROPOSED) }
    }
}
