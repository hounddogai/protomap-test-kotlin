package com.acme.transport.safety.coaching.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingMappingTest {
    @Test
    fun `starts in created`() {
        val item = newCoachingMapping("code-986", "title-211")
        assertEquals(CoachingMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCoachingMapping("code-986", "title-211")
        try {
            item.moveTo(CoachingMappingStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingMappingStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCoachingMapping("code-986", "title-211")
        item.status = CoachingMappingStatus.RETIRED
        assertFails("move") { item.moveTo(CoachingMappingStatus.CREATED) }
    }
}
