package com.acme.transport.safety.coaching.coachingsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCoachingBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalCoachingBaseline("code-49", "title-564")
        assertEquals(GlobalCoachingBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalCoachingBaseline("code-49", "title-564")
        try {
            item.moveTo(GlobalCoachingBaselineStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCoachingBaselineStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalCoachingBaseline("code-49", "title-564")
        item.status = GlobalCoachingBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalCoachingBaselineStatus.REQUESTED) }
    }
}
