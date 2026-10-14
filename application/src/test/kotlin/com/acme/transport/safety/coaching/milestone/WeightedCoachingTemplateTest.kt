package com.acme.transport.safety.coaching.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCoachingTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedCoachingTemplate("code-554", "name-641")
        assertEquals(WeightedCoachingTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newWeightedCoachingTemplate("code-554", "name-641")
        try {
            item.moveTo(WeightedCoachingTemplateStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCoachingTemplateStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedCoachingTemplate("code-554", "name-641")
        item.status = WeightedCoachingTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedCoachingTemplateStatus.DRAFT) }
    }
}
