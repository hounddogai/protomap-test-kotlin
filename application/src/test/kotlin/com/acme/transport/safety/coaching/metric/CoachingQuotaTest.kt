package com.acme.transport.safety.coaching.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingQuotaTest {
    @Test
    fun `starts in created`() {
        val item = newCoachingQuota("code-118", "title-572")
        assertEquals(CoachingQuotaStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCoachingQuota("code-118", "title-572")
        try {
            item.moveTo(CoachingQuotaStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingQuotaStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCoachingQuota("code-118", "title-572")
        item.status = CoachingQuotaStatus.RETIRED
        assertFails("move") { item.moveTo(CoachingQuotaStatus.CREATED) }
    }
}
